package com.lvack.MasterStats.Jobs;

import com.lvack.MasterStats.Db.DataManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * UpdateJobClass for MasterStats
 *
 * @author Leon Vack
 */

@Slf4j
public class UpdateJob implements Job {
    /**
     * generates the champion statistics for all champions, removes old summoner statistics and
     * generates a new overall statistic
     * @param context QuartzScheduler context (not used)
     * @throws JobExecutionException QuartzScheduler exception (not used)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        DataManager.generateChampionStatistics();
        DataManager.clearSummonerStatistics();
        DataManager.generateOverallSummonerStatistic();
    }
}
