package com.lvack.MasterStats.Util;

import java.util.Comparator;
import java.util.Objects;

/**
 * GradeComparatorClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * comparator to order streams of grades
 * the order will be D- -> S+ otherwise
 * also provides a static compare functions for convenience
 */
public class GradeComparator implements Comparator<String> {
    public static int subGradeCompare(String s1, String s2) {
        if (s1.length() == 0) s1 = " ";
        if (s2.length() == 0) s2 = " ";
        return (" ".equals(s1) ? 0 : ("+".equals(s1) ? -1 : +1)) -
                (" ".equals(s2) ? 0 : ("+".equals(s2) ? -1 : +1));
    }

    public static int staticCompare(String s1, String s2) {
        if (Objects.equals(s1, s2)) return 0;
        else if (s1 == null || "null".equals(s1)) return 1;
        else if (s2 == null || "null".equals(s2)) return -1;
        if (s1.length() == 1) s1 += " ";
        if (s2.length() == 1) s2 += " ";
        if (s1.charAt(0) == s2.charAt(0))
            return subGradeCompare(String.valueOf(s1.charAt(1)), String.valueOf(s2.charAt(1)));
            /* (s1.length() > 1 ? s1.charAt(1) == '-' ? +1 : -1 : 0) -
                    (s2.length() > 1 ? s2.charAt(1) == '-' ? +1 : -1 : 0); */
        else if (s1.startsWith("S") || s2.startsWith("S")) return s1.startsWith("S") ? -1 : 1;
        else return s1.charAt(0) - s2.charAt(0);
    }

    @Override
    public int compare(String s1, String s2) {
        return staticCompare(s1, s2);
    }
}
