package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

import java.util.List;

/**
 * MatchListClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class MatchList {
    private int endIndex;
    private List<MatchReference> matches;
    private int startIndex;
    private int totalGames;
}
