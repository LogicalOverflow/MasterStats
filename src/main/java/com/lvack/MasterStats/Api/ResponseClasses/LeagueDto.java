package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

import java.util.List;

/**
 * LeagueDtoClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

@Data
public class LeagueDto {
    private List<LeagueEntryDto> entries;
    private String name;
    private String participantId;
    private String queue;
    private String tier;
}
