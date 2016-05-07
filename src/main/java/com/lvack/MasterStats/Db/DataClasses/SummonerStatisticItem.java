package com.lvack.MasterStats.Db.DataClasses;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.lvack.MasterStats.Db.Marshaller.ChampionMasteryItemListMarshaller;
import com.lvack.MasterStats.Db.Marshaller.SummonerItemMarshaller;
import lombok.Data;

import java.util.List;

/**
 * SummonerStatisticItemClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

@Data
@DynamoDBTable(tableName = "summonerStatistic")
public class SummonerStatisticItem {
    @DynamoDBHashKey(attributeName = "summonerKey")
    private String summonerKey;
    @DynamoDBRangeKey(attributeName = "summonerName")
    private String summonerName;
    @DynamoDBAttribute(attributeName = "summonerItem")
    @DynamoDBMarshalling(marshallerClass = SummonerItemMarshaller.class)
    private SummonerItem summonerItem;
    @DynamoDBAttribute(attributeName = "championMasteries")
    @DynamoDBMarshalling(marshallerClass = ChampionMasteryItemListMarshaller.class)
    private List<ChampionMasteryItem> championMasteries;
    @DynamoDBAttribute(attributeName = "lastUpdated")
    private long lastUpdated;
}
