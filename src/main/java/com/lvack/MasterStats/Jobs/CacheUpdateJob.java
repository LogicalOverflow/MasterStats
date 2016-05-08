package com.lvack.MasterStats.Jobs;

import com.lvack.MasterStats.Db.DBTable;
import com.lvack.MasterStats.Db.DataManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * CacheUpdateJobClass for MasterStats
 *
 * @author Leon Vack
 */

public class CacheUpdateJob implements Job {

    /**
     * updates champions, the champion statistics and the overall statistic in the cache
     * as well as the rate limits for all the tables
     *
     * @param context QuartzScheduler context (not used)
     * @throws JobExecutionException QuartzScheduler exception (not used)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        DBTable.updateAllRateLimits();
        DataManager.updateChampions();
        DataManager.loadChampionData();
        DataManager.loadOverallSummonerStatistic();
    }
}
