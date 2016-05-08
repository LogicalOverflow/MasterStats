package com.lvack.MasterStats.Api.Apis;

import com.lvack.MasterStats.Api.ResponseClasses.ChampionListDto;
import com.lvack.MasterStats.Api.RiotApi;
import com.lvack.MasterStats.Api.RiotApiResponse;
import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;

import javax.ws.rs.client.WebTarget;

/**
 * StaticDataApiClass for MasterStats
 *
 * @author Leon Vack
 */

public class StaticDataApi extends RiotSubApi {
    private static final String BASE_PATH = "/api/lol/static-data/{region}/v1.2/";

    public StaticDataApi(RiotApi riotApi) {
        super(BASE_PATH, riotApi);
    }

    public RiotApiResponse<ChampionListDto> getChampions() {
        WebTarget target = getFunctionTarget("champion")
                .queryParam("champData", "image");
        return processApiResponse(new RiotApiResponse<>(riotApi.prepareRequest(target), ChampionListDto.class));
    }
}
