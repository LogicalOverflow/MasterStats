package com.lvack.MasterStats.Db;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.google.common.util.concurrent.RateLimiter;
import com.lvack.MasterStats.Util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

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
    private HashMap<String, Pair<RateLimiter, RateLimiter>> indexRateLimiter;

    DBTable(String tableName) {
        this.tableName = tableName;
        this.indexRateLimiter = new HashMap<>();
        updateRateLimits();
    }

    /**
     * Updates the read and write capacities for all table
     */
    public static void updateAllRateLimits() {
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
        log.info(String.format(" - %s: %d read, %d write", tableName, readCapacityUnits, writeCapacityUnits));

        // check if secondary indexes exist
        if (table.getGlobalSecondaryIndexes() == null) return;
        // iterate over all indexes and add them to the map of indexes
        table.getGlobalSecondaryIndexes().forEach(i -> {
            Long indexReadCapacityUnits = i.getProvisionedThroughput().getReadCapacityUnits();
            Long indexWriteCapacityUnits = i.getProvisionedThroughput().getWriteCapacityUnits();
            Pair<RateLimiter, RateLimiter> rateLimiters = new Pair<>(RateLimiter.create(indexReadCapacityUnits),
                    RateLimiter.create(indexWriteCapacityUnits));
            indexRateLimiter.put(i.getIndexName(), rateLimiters);
            log.info(String.format("   - %s: %d read, %d write", i.getIndexName(), indexReadCapacityUnits,
                    indexWriteCapacityUnits));
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

    private Pair<RateLimiter, RateLimiter> getIndexRateLimiterPair(String indexName) {
        return indexRateLimiter.get(indexName);
    }
}
