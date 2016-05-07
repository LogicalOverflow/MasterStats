package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * SummonerDtoClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class SummonerDto {
    private long id;
    private String name;
    private int profileIconId;
    private long revisionDate;
    private long summonerLevel;
}
