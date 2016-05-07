package com.lvack.MasterStats.Api;

import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class RiotApiFactory {
    private static String apiKey;
    private static boolean devKey = false;
    private static HashMap<RiotEndpoint, RiotApi> riotApis = new HashMap<>();

    /**
     * creates a new riot api instance with the given region if none exists for the region
     * otherwise just returns the instance for the given region
     * @param endpoint the endpoint the riot api instance should use
     * @return the riot api instance
     */
    public static RiotApi getApi(RiotEndpoint endpoint) {
        if (riotApis.containsKey(endpoint)) return riotApis.get(endpoint);
        log.info(String.format("Building Riot API for %s", endpoint.name()));
        RiotApi riotApi = new RiotApi(endpoint, getApiKey(), isDevKey() ? 0.8 : 150);
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
     * loads the dev key property if needed and returns it
     * @return whether the api key is a development key
     */
    private static boolean isDevKey() {
        if (apiKey == null || apiKey.length() == 0) loadApiKey();
        return devKey;
    }

    /**
     * loads api key and key type from the api.properties file
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
        String devKey = properties.getProperty("devKey", "false");
        RiotApiFactory.devKey = "true".equals(devKey.toLowerCase());
        log.info(String.format("Using API key as a %s key", RiotApiFactory.devKey ? "DEVELOPMENT" : "PRODUCTION"));
    }
}
