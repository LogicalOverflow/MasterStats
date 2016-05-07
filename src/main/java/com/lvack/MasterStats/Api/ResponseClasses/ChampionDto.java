package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * ChampionDtoClass for MasterStats
 *
 * @author Leon Vack
 */

@Data
public class ChampionDto {
    private long id;
    private String name;
    private String title;
    private String key;
    private ImageDto image;
}
