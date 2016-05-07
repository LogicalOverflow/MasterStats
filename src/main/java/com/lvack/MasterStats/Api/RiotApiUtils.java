package com.lvack.MasterStats.Api;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * UtilsClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

public class RiotApiUtils {
    /**
     * converts a list of objects to a comma separated list
     * objects are converted to stings using the toString() method
     * @param es the objects of the list
     * @param <T> the type of the objects
     * @return a string representing the comma separated list
     */
    @SafeVarargs
    public static <T> String arrayToCommaSeparatedList(T... es) {
        return Stream.of(es).map(T::toString).collect(Collectors.joining(","));
    }

}
