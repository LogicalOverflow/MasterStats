package com.lvack.MasterStats.Api.Apis;

import com.google.gson.reflect.TypeToken;
import com.lvack.MasterStats.Api.ResponseClasses.LeagueDto;
import com.lvack.MasterStats.Api.RiotApi;
import com.lvack.MasterStats.Api.RiotApiResponse;
import com.lvack.MasterStats.Api.RiotApiUtils;

import javax.ws.rs.client.WebTarget;
import java.util.HashMap;
import java.util.List;

/**
 * LeagueApiClass for MasterStats
 *
 * @author Leon Vack
 */

public class LeagueApi extends RiotSubApi {
    private static final String BASE_PATH = "/api/lol/{region}/v2.5/league/";

    public LeagueApi(RiotApi riotApi) {
        super(BASE_PATH, riotApi);
    }

    public RiotApiResponse<HashMap<String, List<LeagueDto>>> getLeagueBySummoner(Long... summonerIds) {
        WebTarget target = getFunctionTarget("by-summoner/{summonerIds}")
                .resolveTemplate("summonerIds", RiotApiUtils.arrayToCommaSeparatedList(summonerIds));
        return processApiResponse(new RiotApiResponse<>(riotApi.prepareRequest(target),
                new TypeToken<HashMap<String, List<LeagueDto>>>() {
                }.getType()));
    }
}
