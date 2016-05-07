package com.lvack.MasterStats.Util;

import com.google.gson.Gson;

/**
 * GsonProviderClass for MasterStats
 *
 * @author Leon Vack
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
