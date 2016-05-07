package com.lvack.MasterStats.Db.Marshaller;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * StringIntegerIntegerMapMarshallerClass for RiotApiChallengeChampionMastery
 *
 * @author Leon Vack - TWENTY |20
 */

/**
 * marshaller for Map<String, Map<Integer, Integer>> using the GsonMarshaller but overwriting
 * unmarshall to first read the inner map as a Map<String, Integer> and the convert the keys to integers
 */
public class StringIntegerIntegerMapMarshaller extends GsonMarshaller<Map<String, Map<Integer, Integer>>> {

    @Override
    public Map<String, Map<Integer, Integer>> unmarshall(Class<Map<String, Map<Integer, Integer>>> aClass, String json) {
        Map<String, Map<String, Integer>> stringMapMap = gson.fromJson(json,
                new TypeToken<Map<String, Map<String, Integer>>>() {
                }.getType());
        Map<String, Map<Integer, Integer>> map = new HashMap<>();
        stringMapMap.entrySet().forEach(e -> {
            Map<Integer, Integer> subMap = new HashMap<>();
            e.getValue().entrySet().forEach(i -> subMap.put(Integer.valueOf(i.getKey()), i.getValue()));
            map.put(e.getKey(), subMap);
        });
        return map;
    }
}
