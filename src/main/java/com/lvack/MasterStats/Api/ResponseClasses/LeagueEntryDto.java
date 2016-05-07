package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * LeagueEntryDtoClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class LeagueEntryDto {
    private String division;
    private boolean isFreshBlood;
    private boolean isHotStreak;
    private boolean isInactive;
    private boolean isVeteran;
    private int leaguePoints;
    private int losses;
    private String playerOrTeamId;
    private String playerOrTeamName;
    private int wins;
}
