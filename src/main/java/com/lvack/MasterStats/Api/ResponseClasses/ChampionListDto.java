package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

import java.util.HashMap;

/**
 * ChampionListDtoClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class ChampionListDto {
    private HashMap<String, ChampionDto> data;
    private String format;
    private String type;
    private String version;
}
