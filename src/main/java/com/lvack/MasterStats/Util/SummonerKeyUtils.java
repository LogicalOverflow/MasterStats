package com.lvack.MasterStats.Util;

import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;

/**
 * SummonerKeyUtilClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

public class SummonerKeyUtils {
    /**
     * converts an summoner id and endpoint to a summonerKey (convenience method for db access)
     * @param id the summoners id
     * @param endpoint the endpoint of the summoners region
     * @return the summonerKey for the summoner
     */
    public static String summonerIdRegionToKey(long id, RiotEndpoint endpoint) {
        return summonerIdRegionToKey(id, endpoint.name());
    }

    /**
     * converts an summoner id and endpoint's name to a summonerKey (convenience method for db access)
     * @param id the summoners id
     * @param endpoint the name of the endpoint of the summoners region
     * @return the summonerKey for the summoner
     */
    public static String summonerIdRegionToKey(long id, String endpoint) {
        return String.format("%d_%s", id, endpoint);
    }

    /**
     * extracts the summoner id and endpoint from a summoner key (convenience method for db access)
     * @param key the summonerKey of the summoner
     * @return a SummonerKey object with region and id set matching the data extracted from the key
     */
    public static SummonerKey summonerKeyToIdRegion(String key) {
        String[] split = key.split("_", 2);
        return new SummonerKey(Long.valueOf(split[0]), RiotEndpoint.valueOf(split[1]));
    }
}
