package com.lvack.MasterStats.Jobs;

import com.lvack.MasterStats.Db.DataManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * UpdateChampionsClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

public class CacheUpdateJob implements Job {

    /**
     * updates champions, the champion statistics and the overall statistic in the cache
     * @param context QuartzScheduler context (not used)
     * @throws JobExecutionException QuartzScheduler exception (not used)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        DataManager.updateChampions();
        DataManager.loadChampionData();
        DataManager.loadOverallSummonerStatistic();
    }
}
