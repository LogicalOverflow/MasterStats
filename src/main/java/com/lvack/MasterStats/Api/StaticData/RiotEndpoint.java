package com.lvack.MasterStats.Api.StaticData;

/**
 * RiotEndpointClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * enum of all riot endpoints containing platform id and the host
 */
public enum RiotEndpoint {
    BR("BR1", "br.api.pvp.net"),
    EUNE("EUN1", "eune.api.pvp.net"),
    EUW("EUW1", "euw.api.pvp.net"),
    JP("JP1", "jp.api.pvp.net"),
    KR("KR", "kr.api.pvp.net"),
    LAN("LA1", "lan.api.pvp.net"),
    LAS("LA2", "las.api.pvp.net"),
    NA("NA1", "na.api.pvp.net"),
    OCE("OC1", "oce.api.pvp.net"),
    TR("TR1", "tr.api.pvp.net"),
    RU("RU", "ru.api.pvp.net"),
    PBE("PBE1", "pbe.api.pvp.net"),
    GLOBAL("", "global.api.pvp.net");

    public static final RiotEndpoint[] PLAYABLE_ENDPOINTS = new RiotEndpoint[]{BR, EUNE, EUW, JP, KR, LAN, LAS, NA, OCE, TR, RU};
    public static final RiotEndpoint DEFAULT_ENFPOINT = EUW;
    private final String platformId;
    private final String host;

    RiotEndpoint(String platformId, String host) {
        if (!host.startsWith("http")) host = "https://" + host;
        if (!host.endsWith("/")) host += "/";
        this.platformId = platformId;
        this.host = host;
    }

    public String getPlatformId() {
        return platformId;
    }

    public String getHost() {
        return host;
    }
}
