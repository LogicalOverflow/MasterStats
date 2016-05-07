package com.lvack.MasterStats.Db.DataClasses;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.Data;

/**
 * ChampionMasteryItemClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */
@Data
@DynamoDBTable(tableName = "championMastery")
public class ChampionMasteryItem {
    @DynamoDBHashKey(attributeName = "summonerKey")
    private String summonerKey;
    @DynamoDBRangeKey(attributeName = "championId")
    private long championId;
    @DynamoDBIndexHashKey(attributeName = "championPoints", globalSecondaryIndexName = "championPoints-chestGranted-index")
    private int championPoints;
    @DynamoDBIndexRangeKey(attributeName = "chestGranted", globalSecondaryIndexName = "championPoints-chestGranted-index")
    private int chestGranted;
    @DynamoDBAttribute(attributeName = "championLevel")
    private int championLevel;
    @DynamoDBAttribute(attributeName = "championPointsSinceLastLevel")
    private int championPointsSinceLastLevel;
    @DynamoDBAttribute(attributeName = "championPointsUntilNextLevel")
    private int championPointsUntilNextLevel;
    @DynamoDBAttribute(attributeName = "highestGrade")
    private String highestGrade;
    @DynamoDBAttribute(attributeName = "lastPlayTime")
    private long lastPlayTime;
}
