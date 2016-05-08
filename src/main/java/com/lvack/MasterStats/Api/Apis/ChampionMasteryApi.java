package com.lvack.MasterStats.Api.Apis;

import com.google.gson.reflect.TypeToken;
import com.lvack.MasterStats.Api.ResponseClasses.ChampionMasteryDto;
import com.lvack.MasterStats.Api.RiotApi;
import com.lvack.MasterStats.Api.RiotApiResponse;

import javax.ws.rs.client.WebTarget;
import java.util.List;

/**
 * ChampionMasteryApiClass for MasterStats
 *
 * @author Leon Vack
 */

public class ChampionMasteryApi extends RiotSubApi {
    private static final String BASE_PATH = "/championmastery/location/{platformId}/player/";

    public ChampionMasteryApi(RiotApi riotApi) {
        super(BASE_PATH, riotApi);
    }

    public RiotApiResponse<ChampionMasteryDto> getSummonersChampionMasteryByChampion(long summonerId, long championId) {
        WebTarget target = getFunctionTarget("{summonerId}/champion/{championId}")
                .resolveTemplate("summonerId", summonerId)
                .resolveTemplate("championId", championId);
        return processApiResponse(new RiotApiResponse<>(riotApi.prepareRequest(target), ChampionMasteryDto.class));
    }

    public RiotApiResponse<List<ChampionMasteryDto>> getSummonersChampionMastery(long summonerId) {
        WebTarget target = getFunctionTarget("{summonerId}/champions")
                .resolveTemplate("summonerId", summonerId);
        return processApiResponse(new RiotApiResponse<>(riotApi.prepareRequest(target), new TypeToken<List<ChampionMasteryDto>>() {
        }.getType()));
    }

    public RiotApiResponse<Integer> getSummonersChampionMasteryScore(long summonerId) {
        WebTarget target = getFunctionTarget("{summonerId}/score")
                .resolveTemplate("summonerId", summonerId);
        return processApiResponse(new RiotApiResponse<>(riotApi.prepareRequest(target), Integer.class));
    }

    public RiotApiResponse<List<ChampionMasteryDto>> getSummonersChampionMasteryTopChampions(long summonerId) {
        WebTarget target = getFunctionTarget("{summonerId}/topchampions")
                .resolveTemplate("summonerId", summonerId);
        return processApiResponse(new RiotApiResponse<>(riotApi.prepareRequest(target), new TypeToken<List<ChampionMasteryDto>>() {
        }.getType()));
    }

    public RiotApiResponse<List<ChampionMasteryDto>> getSummonersChampionMasteryTopChampions(long summonerId, int count) {
        WebTarget target = getFunctionTarget("{summonerId}/topchampions")
                .resolveTemplate("summonerId", summonerId)
                .queryParam("count", count);
        return processApiResponse(new RiotApiResponse<>(riotApi.prepareRequest(target), new TypeToken<List<ChampionMasteryDto>>() {
        }.getType()));
    }

}
