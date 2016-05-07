package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * MatchReferenceClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class MatchReference {
    private long champion;
    private String lane;
    private long matchId;
    private String platformId;
    private String queue;
    private String region;
    private String role;
    private String season;
    private long timestamp;
}
