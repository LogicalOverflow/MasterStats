package com.lvack.MasterStats.Api.Apis;

import com.lvack.MasterStats.Api.RiotApi;
import com.lvack.MasterStats.Api.RiotApiResponse;

import javax.ws.rs.client.WebTarget;

/**
 * RiotSubApiClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

abstract class RiotSubApi {
    final RiotApi riotApi;
    private final String basePath;

    RiotSubApi(String basePath, RiotApi riotApi) {
        this.basePath = basePath;
        this.riotApi = riotApi;
    }

    /**
     * returns a web target for with the correct endpoint and provided path
     * @param path path for the web target
     * @return a fresh web target
     */
    WebTarget getFunctionTarget(String path) {
        return riotApi.buildEndpointTarget().path(basePath + path);
    }

    /**
     * processes a riot api response by setting the rate limiter and sending the get request
     * @param response the response to process
     * @param <T> the return type of the response
     * @return the process response
     */
    protected <T> RiotApiResponse<T> processApiResponse(RiotApiResponse<T> response) {
        response.setRateLimiter(riotApi.getRateLimiter());
        response.sendGet();
        return response;
    }
}
