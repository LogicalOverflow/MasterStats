package com.lvack.MasterStats.Api.ResponseClasses;

import lombok.Data;

/**
 * ImageDtoClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

@Data
public class ImageDto {
    private String full;
    private String group;
    private int h;
    private String sprite;
    private int w;
    private int x;
    private int y;
}
