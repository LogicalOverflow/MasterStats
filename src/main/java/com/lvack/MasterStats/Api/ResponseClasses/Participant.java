package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * ParticipantClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class Participant {
    private int championId;
    private String highestAchievedSeasonTier;
    private int participantId;
    private int teamId;
}
