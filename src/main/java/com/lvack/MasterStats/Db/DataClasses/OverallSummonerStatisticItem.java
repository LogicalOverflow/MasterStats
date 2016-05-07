package com.lvack.MasterStats.Db.DataClasses;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.lvack.MasterStats.Db.Marshaller.StringIntegerIntegerMapMarshaller;
import lombok.Data;

import java.util.HashMap;

/**
 * OverallSummonerStatisticItemClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

@Data
@DynamoDBTable(tableName = "summonerStatistic")
public class OverallSummonerStatisticItem {
    public static final String OVERALL_KEY = "overall";
    @DynamoDBHashKey(attributeName = "summonerKey")
    private String summonerKey = OVERALL_KEY;
    @DynamoDBRangeKey(attributeName = "summonerName")
    private String summonerName = OVERALL_KEY;
    @DynamoDBAttribute(attributeName = "summonerCounts")
    private HashMap<String, Integer> summonerCounts;
    @DynamoDBAttribute(attributeName = "masteryScoreCounts")
    @DynamoDBMarshalling(marshallerClass = StringIntegerIntegerMapMarshaller.class)
    private HashMap<String, HashMap<Integer, Integer>> masteryScoreCounts;
    @DynamoDBAttribute(attributeName = "tierCounts")
    private HashMap<String, HashMap<String, Integer>> tierCounts;

}
