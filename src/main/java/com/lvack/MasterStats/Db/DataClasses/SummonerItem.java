package com.lvack.MasterStats.Db.DataClasses;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.Data;

/**
 * SummonerItemClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
@DynamoDBTable(tableName = "summoner")
public class SummonerItem {
    @DynamoDBHashKey(attributeName = "summonerKey")
    private String summonerKey;
    @DynamoDBRangeKey(attributeName = "summonerName")
    private String summonerName;
    @DynamoDBIndexHashKey(attributeName = "division", globalSecondaryIndexName = "division-tier-index")
    private String division;
    @DynamoDBIndexRangeKey(attributeName = "tier", globalSecondaryIndexName = "division-tier-index")
    private String tier;
    @DynamoDBIndexHashKey(attributeName = "masteryScore", globalSecondaryIndexName = "masteryScore-lastUpdated-index")
    private int masteryScore;
    @DynamoDBIndexRangeKey(attributeName = "lastUpdated", globalSecondaryIndexName = "masteryScore-lastUpdated-index")
    private long lastUpdated;
    @DynamoDBAttribute(attributeName = "profileIconId")
    private int profileIconId;
    @DynamoDBAttribute(attributeName = "revisionDate")
    private long revisionDate;
    @DynamoDBAttribute(attributeName = "summonerLevel")
    private long summonerLevel;

}
