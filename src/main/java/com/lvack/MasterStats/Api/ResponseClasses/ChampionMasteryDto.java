package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * ChampionMasteryDtoClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class ChampionMasteryDto {
    private long championId;
    private int championLevel;
    private int championPoints;
    private int championPointsSinceLastLevel;
    private int championPointsUntilNextLevel;
    private boolean chestGranted;
    private String highestGrade;
    private long lastPlayTime;
    private long playerId;
}
