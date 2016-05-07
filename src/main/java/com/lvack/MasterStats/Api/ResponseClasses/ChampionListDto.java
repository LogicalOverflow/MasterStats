package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

import java.util.HashMap;

/**
 * ChampionListClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

@Data
public class ChampionListDto {
    private HashMap<String, ChampionDto> data;
    private String format;
    private String type;
    private String version;
}
