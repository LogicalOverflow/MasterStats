package com.lvack.MasterStats.Api.StaticData;

import com.googlecode.wickedcharts.highcharts.options.color.ColorReference;
import com.googlecode.wickedcharts.highcharts.options.color.HexColor;
import lombok.extern.slf4j.Slf4j;

/**
 * TierNamesClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * enum of all ranked tier names
 */
@Slf4j
public enum RankedTier {
    CHALLENGER(new HexColor("#FACC81"), 7),
    MASTER(new HexColor("#6F9796"), 6),
    DIAMOND(new HexColor("#50BBDB"), 5),
    PLATINUM(new HexColor("#4D9895"), 4),
    GOLD(new HexColor("#E9DB97"), 3),
    SILVER(new HexColor("#BDCAC2"), 2),
    BRONZE(new HexColor("#8D633E"), 1),
    UNRANKED(new HexColor("#A8A8A8"), 0);

    private final ColorReference color;
    private final int id;
    RankedTier(ColorReference color, int id) {
        this.color = color;
        this.id = id;
    }

    /**
     * returns the tier with the given name, if none exists UNRANKED is returned
     * @param name the name of the tier
     * @return the tier object for the given name
     */
    public static RankedTier getTierByName(String name) {
        try {
            return RankedTier.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNRANKED;
        }
    }

    public ColorReference getColor() {
        return color;
    }

    public int getId() {
        return id;
    }
}
