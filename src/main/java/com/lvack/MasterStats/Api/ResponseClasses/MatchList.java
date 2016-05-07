package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

import java.util.List;

/**
 * MatchListClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

@Data
public class MatchList {
    private int endIndex;
    private List<MatchReference> matches;
    private int startIndex;
    private int totalGames;
}
