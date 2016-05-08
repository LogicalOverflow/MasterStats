package com.lvack.MasterStats.Util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * PairClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * simplistic utility class representing a key-value pair
 *
 * @param <K> type of the key
 * @param <V> type of the value
 */
@Data
@AllArgsConstructor
public class Pair<K, V> {
    private K key;
    private V value;
}
