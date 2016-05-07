package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * SummonerDtoClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

@Data
public class SummonerDto {
    private long id;
    private String name;
    private int profileIconId;
    private long revisionDate;
    private long summonerLevel;
}
