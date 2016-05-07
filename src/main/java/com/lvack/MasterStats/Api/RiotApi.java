package com.lvack.MasterStats.Api;

import com.google.common.util.concurrent.RateLimiter;
import com.lvack.MasterStats.Api.Apis.*;
import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * RiotApiClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

public class RiotApi {
    private final String apiKey;
    private final Client client;
    private final RiotEndpoint endpoint;
    private SummonerApi summonerApi;
    private ChampionMasteryApi championMasteryApi;
    private StaticDataApi staticDataApi;
    private MatchApi matchApi;
    private LeagueApi leagueApi;
    private MatchListApi matchListApi;
    private RateLimiter rateLimiter;

    /**
     * create a rate limiter and a jersey client to manage request
     * @param endpoint the endpoint to send the requests to
     * @param apiKey the api key to use
     */
    public RiotApi(RiotEndpoint endpoint, String apiKey) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        rateLimiter = RateLimiter.create(150);
        client = ClientBuilder.newBuilder()
                .build();
    }

    /**
     * returns a async invoke provider for a given target which set api key, region and platform id for the target
     * @param target the target to make the requests to
     * @return a async invoke provider for a given target
     */
    public RiotApiResponse.AsyncInvokerProvider prepareRequest(WebTarget target) {
        return () -> {
            WebTarget webTarget = target
                    .queryParam("api_key", apiKey)
                    .resolveTemplate("region", endpoint.name().toLowerCase())
                    .resolveTemplate("platformId", endpoint.getPlatformId());
            return webTarget.request().accept(MediaType.APPLICATION_JSON_TYPE).async();
        };
    }


    /**
     * creates a new web target for the endpoint
     * @return a new web target
     */
    public WebTarget buildEndpointTarget() {
        return client.target(endpoint.getHost());
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    /**
     * the following functions are all getter for the sub apis which create the api if it is not set already
     * and returns it
     * @return the sub api
     */
    public SummonerApi getSummonerApi() {
        if (summonerApi == null) summonerApi = new SummonerApi(this);
        return summonerApi;
    }

    public ChampionMasteryApi getChampionMasteryApi() {
        if (championMasteryApi == null) championMasteryApi = new ChampionMasteryApi(this);
        return championMasteryApi;
    }

    public StaticDataApi getStaticDataApi() {
        if (staticDataApi == null) staticDataApi = new StaticDataApi(this);
        return staticDataApi;
    }

    public MatchApi getMatchApi() {
        if (matchApi == null) matchApi = new MatchApi(this);
        return matchApi;
    }

    public LeagueApi getLeagueApi() {
        if (leagueApi == null) leagueApi = new LeagueApi(this);
        return leagueApi;
    }

    public MatchListApi getMatchListApi() {
        if (matchListApi == null) matchListApi = new MatchListApi(this);
        return matchListApi;
    }
}
