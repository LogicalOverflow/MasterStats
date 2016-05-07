package com.lvack.MasterStats.Api;

import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * RiotApiFactoryClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * Class holding and creating the instance of the riot api (one per region)
 */
public class RiotApiFactory {
    private static String apiKey;
    private static HashMap<RiotEndpoint, RiotApi> riotApis = new HashMap<>();

    /**
     * creates a new riot api instance with the given region if none exists for the region
     * otherwise just returns the instance for the given region
     * @param endpoint the endpoint the riot api instance should use
     * @return the riot api instance
     */
    public static RiotApi getApi(RiotEndpoint endpoint) {
        if (riotApis.containsKey(endpoint)) return riotApis.get(endpoint);
        RiotApi riotApi = new RiotApi(endpoint, getApiKey());
        riotApis.put(endpoint, riotApi);
        return riotApi;
    }

    /**
     * loads the api key if needed and returns it
     * @return the api key
     */
    private static String getApiKey() {
        if (apiKey == null || apiKey.length() == 0) loadApiKey();
        return apiKey;
    }

    /**
     * loads api key from the api.properties file
     */
    private static void loadApiKey() {
        Properties properties = new Properties();
        InputStream in = RiotApiFactory.class.getClassLoader().getResourceAsStream("api.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        apiKey = properties.getProperty("apiKey");
    }
}
