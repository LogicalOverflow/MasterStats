package com.lvack.MasterStats.Db.DataClasses;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.google.gson.Gson;
import com.lvack.MasterStats.Db.Marshaller.StringIntegerIntegerMapMarshaller;
import com.lvack.MasterStats.Db.Marshaller.SummonerItemChampionMasteryItemPairListMarshaller;
import com.lvack.MasterStats.Util.GsonProvider;
import com.lvack.MasterStats.Util.Pair;
import lombok.Data;

import java.util.*;

/**
 * ChampionStatisticItemClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
@DynamoDBTable(tableName = "championStatistic")
public class ChampionStatisticItem {
    public static final int SCORE_DISTRIBUTION_STEP_SIZE = 100;
    public static final int CHAMPION_SCORE_STEP_COUNT = 250;
    public static final int MIN_CHAMPION_LEVEL = 1;
    public static final int MAX_CHAMPION_LEVEL = 5;
    public static Set<String> GRADES = new HashSet<>(Arrays.asList("S+", "S", "S-",
            "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "null"));
    public static final int TOP_SUMMONER_COUNT = 20;

    private static Gson gson = GsonProvider.getGSON();


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

    @DynamoDBAttribute(attributeName = "avgMasteryPoints")
    private double avgMasteryPoints;
    @DynamoDBAttribute(attributeName = "sumMasteryPoints")
    private long sumMasteryPoints;
    @DynamoDBAttribute(attributeName = "thresholdMasteryPoints")
    private long thresholdMasteryPoints;

    @DynamoDBAttribute(attributeName = "topSummoners")
    @DynamoDBMarshalling(marshallerClass = SummonerItemChampionMasteryItemPairListMarshaller.class)
    private List<Pair<SummonerItem, ChampionMasteryItem>> topSummoners;

    @DynamoDBAttribute(attributeName = "highestGradeCounts")
    private Map<String, Map<String, Integer>> highestGradeCounts = new HashMap<>();
    @DynamoDBAttribute(attributeName = "levelCounts")
    @DynamoDBMarshalling(marshallerClass = StringIntegerIntegerMapMarshaller.class)
    private Map<String, Map<Integer, Integer>> levelCounts = new HashMap<>();
    @DynamoDBAttribute(attributeName = "playerCount")
    private Map<String, Integer> playerCount = new HashMap<>();
    @DynamoDBAttribute(attributeName = "chestsGranted")
    private Map<String, Integer> chestsGranted = new HashMap<>();
    @DynamoDBAttribute(attributeName = "scoreDistribution")
    @DynamoDBMarshalling(marshallerClass = StringIntegerIntegerMapMarshaller.class)
    private Map<String, Map<Integer, Integer>> scoreDistribution = new HashMap<>();
}
