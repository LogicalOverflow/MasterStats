package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

import java.util.List;

/**
 * LeagueDtoClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class LeagueDto {
    private List<LeagueEntryDto> entries;
    private String name;
    private String participantId;
    private String queue;
    private String tier;
}
