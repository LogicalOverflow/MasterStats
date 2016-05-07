package com.lvack.MasterStats.Util;

/**
 * TierComparatorClass for MasterStats
 *
 * @author Leon Vack
 */

import com.lvack.MasterStats.Api.StaticData.RankedTier;

import java.util.Comparator;

/**
 * comparator to order streams of tiers
 * the order will be null/UNRANKED -> BRONZE -> CHALLENGER otherwise
 * also provides a static compare function for convenience
 */
public class TierComparator implements Comparator<String> {
    public static int staticCompare(String s1, String s2) {
        return staticCompare(RankedTier.getTierByName(s1), RankedTier.getTierByName(s2));
    }


    public static int staticCompare(RankedTier t1, RankedTier t2) {
        return t1.getId() - t2.getId();
    }

    @Override
    public int compare(String s1, String s2) {
        return staticCompare(s1, s2);
    }
}
