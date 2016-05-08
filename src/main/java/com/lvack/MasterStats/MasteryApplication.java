package com.lvack.MasterStats;

import com.lvack.MasterStats.Db.DataManager;
import com.lvack.MasterStats.Jobs.CacheUpdateJob;
import com.lvack.MasterStats.Jobs.SummonerCrawlRunnable;
import com.lvack.MasterStats.Jobs.UpdateJob;
import com.lvack.MasterStats.Pages.ErrorPages.Error404Page;
import com.lvack.MasterStats.Pages.ErrorPages.ServerErrorPage;
import com.lvack.MasterStats.Pages.HomePage;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.protocol.http.WebApplication;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

/**
 * MasteryApplicationClass for MasterStats
 *
 * @author Leon Vack
 */

@Slf4j
public class MasteryApplication extends WebApplication {
    // boolean to easily toggle deployment between deployment and development mode
    private static final boolean deployment = true;
    private Scheduler scheduler;
    private SummonerCrawlRunnable summonerCrawlRunnable;


    @Override

    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    protected void init() {
        super.init();

        log.info(String.format("Application is starting in %s mode", getConfigurationType().name()));

        // load annotated pages
        new AnnotatedMountScanner().scanPackage("com.lvack.MasterStats.Pages").mount(this);
        try {
            Class.forName("com.lvack.MasterStats.Db.DBConnector");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // set error pages
        getApplicationSettings().setPageExpiredErrorPage(ServerErrorPage.class);
        getApplicationSettings().setAccessDeniedPage(ServerErrorPage.class);
        getApplicationSettings().setInternalErrorPage(ServerErrorPage.class);
        mountPage("/error404", Error404Page.class);

        log.info("Executing nightly update job");

        // execute nightly job to generate data needed
        try {
            new CacheUpdateJob().execute(null);
        } catch (JobExecutionException e) {
            e.printStackTrace();
        }

        // only schedule jobs and summoner crawl in deployment mode
        if (deployment) {
            log.info("Scheduling Jobs");

            // create a quartz job scheduler and schedule jobs
            try {
                scheduler = StdSchedulerFactory.getDefaultScheduler();

                JobDetail cacheUpdateJob = JobBuilder.newJob(CacheUpdateJob.class)
                        .withIdentity("defaultCacheUpdater", "cacheUpdater")
                        .build();

                Trigger cacheUpdateTrigger = TriggerBuilder.newTrigger()
                        .withIdentity("defaultCacheUpdaterTrigger", "cacheUpdater")
                        .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(3, 50))
                        .build();

                scheduler.scheduleJob(cacheUpdateJob, cacheUpdateTrigger);

                JobDetail updateJob = JobBuilder.newJob(UpdateJob.class)
                        .withIdentity("defaultUpdater", "updater")
                        .build();

                Trigger updateTrigger = TriggerBuilder.newTrigger()
                        .withIdentity("defaultUpdaterTrigger", "updater")
                        .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(4, 0))
                        .build();

                scheduler.scheduleJob(updateJob, updateTrigger);

                scheduler.start();
            } catch (SchedulerException e) {
                e.printStackTrace();
            }

            log.info("Starting summoner crawl thread");

            // start a thread to crawl new summoners
            summonerCrawlRunnable = new SummonerCrawlRunnable();
            Thread summonerThread = new Thread(summonerCrawlRunnable);
            summonerThread.setName("defaultSummonerCrawlThread");
            summonerThread.start();

            log.info("Starting overall summoner statistic update thread");

            // start a thread to update the overall summoner statistic
            Thread overallThread = new Thread(DataManager::generateOverallSummonerStatistic);
            overallThread.setName("startupOverallSummonerStatisticUpdateThread");
            overallThread.start();
        }

        log.info("Application initialization completed");
    }

    @Override
    public RuntimeConfigurationType getConfigurationType() {
        //noinspection ConstantConditions
        return deployment ? RuntimeConfigurationType.DEPLOYMENT : RuntimeConfigurationType.DEVELOPMENT;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // shut quartz scheduler down
        if (scheduler != null) try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        // stop summoner crawler
        if (summonerCrawlRunnable != null) summonerCrawlRunnable.terminate();
    }
}
