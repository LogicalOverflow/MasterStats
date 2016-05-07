package com.lvack.MasterStats.Api.Apis;

import com.lvack.MasterStats.Api.ResponseClasses.MatchList;
import com.lvack.MasterStats.Api.RiotApi;
import com.lvack.MasterStats.Api.RiotApiResponse;

import javax.ws.rs.client.WebTarget;

/**
 * MatchListApiClass for MasterStats
 *
 * @author Leon Vack
 */

public class MatchListApi extends RiotSubApi {
    private static final String BASE_PATH = "/api/lol/{region}/v2.2/matchlist/";

    public MatchListApi(RiotApi riotApi) {
        super(BASE_PATH, riotApi);
    }

    public RiotApiResponse<MatchList> getMatchListBySummoner(Long summonerId, int beginIndex, int endIndex) {
        WebTarget target = getFunctionTarget("by-summoner/{summonerId}")
                .resolveTemplate("summonerId", summonerId)
                .queryParam("beginIndex", beginIndex)
                .queryParam("endIndex", endIndex);
        return processApiResponse(new RiotApiResponse<>(riotApi.prepareRequest(target), MatchList.class));
    }
}
