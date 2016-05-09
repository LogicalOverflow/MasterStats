package com.lvack.MasterStats.Db;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.google.common.util.concurrent.RateLimiter;
import com.lvack.MasterStats.Util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * DBTableClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * enum of all db tables with their respective name and rate limiters to control request counts
 */
@Slf4j
public enum DBTable {
    CHAMPION("champion"),
    CHAMPION_MASTERY("championMastery"),
    CHAMPION_STATISTIC("championStatistic"),
    SUMMONER("summoner"),
    SUMMONER_STATISTIC("summonerStatistic");
    private final String tableName;
    private RateLimiter readLimiter;
    private RateLimiter writeLimiter;
    private HashMap<String, Pair<RateLimiter, RateLimiter>> indexRateLimiters;

    DBTable(String tableName) {
        this.tableName = tableName;
        this.indexRateLimiters = new HashMap<>();
        updateRateLimits();
    }

    /**
     * Updates the read and write capacities for all table
     */
    public static void updateAllRateLimits() {
        log.info("updating dynamoDB rate limits");
        for (DBTable dbTable : DBTable.values()) dbTable.updateRateLimits();
    }

    /**
     * Updates the read and write capacities for the table
     */
    public void updateRateLimits() {
        // request table data from the db
        DynamoDB dynamoDB = DBConnector.getInstance().getDynamoDB();
        TableDescription table = dynamoDB.getTable(tableName).describe();

        // get table read and write limiter
        Long readCapacityUnits = table.getProvisionedThroughput().getReadCapacityUnits();
        Long writeCapacityUnits = table.getProvisionedThroughput().getWriteCapacityUnits();

        // update rate limits
        readLimiter = RateLimiter.create(readCapacityUnits);
        writeLimiter = RateLimiter.create(writeCapacityUnits);

        // check if secondary indexes exist
        if (table.getGlobalSecondaryIndexes() == null) return;
        // iterate over all indexes and add them to the map of indexes
        table.getGlobalSecondaryIndexes().forEach(i -> {
            Long indexReadCapacityUnits = i.getProvisionedThroughput().getReadCapacityUnits();
            Long indexWriteCapacityUnits = i.getProvisionedThroughput().getWriteCapacityUnits();
            Pair<RateLimiter, RateLimiter> rateLimiters = new Pair<>(RateLimiter.create(indexReadCapacityUnits),
                    RateLimiter.create(indexWriteCapacityUnits));
            indexRateLimiters.put(i.getIndexName(), rateLimiters);
        });
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

    public RateLimiter getIndexReadLimiter(String indexName) {
        return getIndexRateLimiterPair(indexName).getKey();
    }

    public RateLimiter getIndexWriteLimiter(String indexName) {
        return getIndexRateLimiterPair(indexName).getValue();
    }

    public Set<String> getIndexNames() {
        return indexRateLimiters.keySet();
    }

    private Pair<RateLimiter, RateLimiter> getIndexRateLimiterPair(String indexName) {
        return indexRateLimiters.get(indexName);
    }
}
