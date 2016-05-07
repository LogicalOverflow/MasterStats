package com.lvack.MasterStats.Util;

import com.google.gson.Gson;

/**
 * GsonProviderClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * static class to provide the global gson instance
 */
public class GsonProvider {
    private static Gson GSON = new Gson();

    public static Gson getGSON() {
        return GSON;
    }
}
