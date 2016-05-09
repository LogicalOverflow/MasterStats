package com.lvack.MasterStats.Jobs;

import com.lvack.MasterStats.Db.DBTable;
import com.lvack.MasterStats.Db.DataManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * CacheUpdateJobClass for MasterStats
 *
 * @author Leon Vack
 */

@Slf4j
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
        // log limits used
        for (DBTable dbTable : DBTable.values()) {
            log.info(String.format(" - %s: %d read, %d write", dbTable.getTableName(),
                    (int) dbTable.getReadLimiter().getRate(), (int) dbTable.getWriteLimiter().getRate()));
            dbTable.getIndexNames().forEach(i -> log.info(String.format("   - %s: %d read, %d write", i,
                    (int) dbTable.getIndexReadLimiter(i).getRate(), (int) dbTable.getIndexWriteLimiter(i).getRate())));
        }
        DataManager.updateChampions();
        DataManager.loadChampionData();
        DataManager.loadOverallSummonerStatistic();
    }
}
