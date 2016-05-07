package com.lvack.MasterStats.Db;

import com.google.common.util.concurrent.RateLimiter;

/**
 * TableClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * enum of all db tables with their respective name and rate limiters to control request counts
 */
public enum DBTable {
    CHAMPION("champion", 1, 1),
    CHAMPION_MASTERY("championMastery", 10, 10),
    CHAMPION_STATISTIC("championStatistic", 1, 1),
    SUMMONER("summoner", 5, 1),
    SUMMONER_STATISTIC("summonerStatistic", 1, 1);
    private final String tableName;
    private final RateLimiter readLimiter;
    private final RateLimiter writeLimiter;

    DBTable(String tableName, double readLimit, double writeLimit) {
        this.tableName = tableName;
        this.readLimiter = RateLimiter.create(readLimit);
        this.writeLimiter = RateLimiter.create(writeLimit);
    }

    public String getTableName() {
        return tableName;
    }

    public RateLimiter getReadLimiter() {
        return readLimiter;
    }

    public RateLimiter getWriteLimiter() {
        return writeLimiter;
    }
}
