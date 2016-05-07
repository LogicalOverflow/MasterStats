package com.lvack.MasterStats.Util;

import com.lvack.MasterStats.Api.StaticData.RiotEndpoint;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * SummonerKeyClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * simplistic utility class to store a pair of summoner id and region
 * used by the SummonerKeyUtils
 */
@Data
@AllArgsConstructor
public class SummonerKey {
    private long id;
    private RiotEndpoint region;
}
