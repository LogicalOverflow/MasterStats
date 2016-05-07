package com.lvack.MasterStats.Api.Apis;

import com.lvack.MasterStats.Api.ResponseClasses.MatchDetail;
import com.lvack.MasterStats.Api.RiotApi;
import com.lvack.MasterStats.Api.RiotApiResponse;

import javax.ws.rs.client.WebTarget;

/**
 * MatchApiClass for MasterStats
 *
 * @author Leon Vack
 */

public class MatchApi extends RiotSubApi {
    private static final String BASE_PATH = "/api/lol/{region}/v2.2/match/";

    public MatchApi(RiotApi riotApi) {
        super(BASE_PATH, riotApi);
    }

    public RiotApiResponse<MatchDetail> getMatchById(Long matchId) {
        WebTarget target = getFunctionTarget("{matchId}")
                .resolveTemplate("matchId", matchId);
        return processApiResponse(new RiotApiResponse<>(riotApi.prepareRequest(target), MatchDetail.class));
    }
}
