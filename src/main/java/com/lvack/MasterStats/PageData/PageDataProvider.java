package com.lvack.MasterStats.PageData;

import com.lvack.MasterStats.Api.ResponseClasses.SummonerDto;
import com.lvack.MasterStats.Api.RiotApi;
import com.lvack.MasterStats.Api.RiotApiFactory;
import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;
import com.lvack.MasterStats.Db.DataClasses.ChampionStatisticItem;
import com.lvack.MasterStats.Db.DataClasses.OverallSummonerStatisticItem;
import com.lvack.MasterStats.Db.DataClasses.SummonerStatisticItem;
import com.lvack.MasterStats.Db.DataManager;
import com.lvack.MasterStats.Util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.HashMap;
import java.util.Map;

/**
 * PageDataProviderClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * class used by page classes to get their data to display
 */
@Slf4j
public class PageDataProvider {
    public static final CircularFifoQueue<Pair<String, SummonerStatisticItem>> summonerStatisticCache = new CircularFifoQueue<>(1024);
    public static Map<String, ChampionStatisticItem> championStatisticMap = new HashMap<>();
    public static Map<Long, String> championIdKeyNameMap = new HashMap<>();
    public static OverallSummonerStatisticItem overallSummonerStatisticItem;
    public static String version;

    /**
     * gets the champion statistic for a champion from the local cache, returns null if it was not found
     *
     * @param championId the id of the champion
     * @return the given champions statistic, null if not found in local cache
     */
    public static ChampionStatisticItem getChampionStatisticById(long championId) {
        String key = championIdKeyNameMap.getOrDefault(championId, "").toLowerCase();
        if (championStatisticMap.containsKey(key)) return championStatisticMap.get(key);
        return null;
    }

    /**
     * gets or generates the summoner statistic for a given summoner
     *
     * @param summonerName the summoners name
     * @param region       the summoners region
     * @return a pair with the summoners name key as key and the statistic as value
     */
    public static Pair<String, SummonerStatisticItem> generateSummonerStatistic(String summonerName, RiotEndpoint region) {
        // get the api and get the summoners data
        RiotApi riotApi = RiotApiFactory.getApi(region);
        Map<String, SummonerDto> summonerDtos = riotApi.getSummonerApi().getSummonersByNames(summonerName).get();

        // if the summoner was not found or multiple were found, return null
        if (summonerDtos == null || summonerDtos.size() != 1) return null;
        Map.Entry<String, SummonerDto> summonerDtoEntry = summonerDtos.entrySet().stream().findFirst().orElse(null);
        if (summonerDtoEntry == null) return null;

        // get the summoners name key
        String summonerNameKey = summonerDtoEntry.getKey();
        // check if the summoners statistic is stored in the cache
        Pair<String, SummonerStatisticItem> summonerStatistic = summonerStatisticCache.stream()
                .filter(p -> p.getKey().equals(summonerNameKey) && p.getValue().getSummonerKey().endsWith(region.name()))
                .findFirst().orElse(null);
        // if it is stored in the cache and not older the the up-to-date duration remove it and
        // add it again at the beginning as the cache removes the oldest element when it one is added while it is full
        // and then return it
        if (summonerStatistic != null &&
                System.currentTimeMillis() - summonerStatistic.getValue().getLastUpdated() < DataManager.UP_TO_DATE_DURATION) {
            summonerStatisticCache.remove(summonerStatistic);
            summonerStatisticCache.add(summonerStatistic);
            return summonerStatistic;
        }

        // get the statistic from the db or generate it
        SummonerStatisticItem statistic = DataManager.getSummonerStatistic(summonerDtoEntry.getValue(), region);

        // if the summoner statistic could not be generated return null
        if (statistic == null) return null;
        // add the up-to-date statistic to a pair with the summoner name key as key and the statistic as value
        summonerStatistic = new Pair<>(summonerNameKey, statistic);
        // add it to the cache and return it
        summonerStatisticCache.add(summonerStatistic);
        return summonerStatistic;
    }
}
