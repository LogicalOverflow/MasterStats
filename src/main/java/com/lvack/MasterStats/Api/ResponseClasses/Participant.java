package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * ParticipantClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

@Data
public class Participant {
    private int championId;
    private String highestAchievedSeasonTier;
    private int participantId;
    private int teamId;
}
