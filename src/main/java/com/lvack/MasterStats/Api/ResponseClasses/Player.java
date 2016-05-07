package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * PlayerClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

@Data
public class Player {
    private String matchHistoryUri;
    private int profileIcon;
    private long summonerId;
    private String summonerName;
}
