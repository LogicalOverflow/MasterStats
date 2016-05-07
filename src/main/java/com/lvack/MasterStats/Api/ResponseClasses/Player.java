package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * PlayerClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class Player {
    private String matchHistoryUri;
    private int profileIcon;
    private long summonerId;
    private String summonerName;
}
