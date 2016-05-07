package com.lvack.MasterStats.Db.DataClasses;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Data;

/**
 * ChampionItemClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

@Data
@DynamoDBTable(tableName = "champion")
public class ChampionItem {
    @DynamoDBHashKey(attributeName = "keyName")
    private String keyName;
    @DynamoDBRangeKey(attributeName = "championId")
    private long championId;
    @DynamoDBAttribute(attributeName = "championName")
    private String championName;
    @DynamoDBAttribute(attributeName = "championTitle")
    private String championTitle;
    @DynamoDBAttribute(attributeName = "portraitUrl")
    private String portraitUrl;
}
