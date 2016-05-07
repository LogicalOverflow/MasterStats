package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

import java.util.List;

/**
 * MatchDetailClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class MatchDetail {
    private int mapId;
    private long matchCreation;
    private long matchDuration;
    private long matchId;
    private String matchMode;
    private String matchVersion;
    private List<ParticipantIdentity> participantIdentities;
    private String platformId;
    private String queueType;
    private String region;
    private String season;
}
