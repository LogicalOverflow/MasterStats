package com.lvack.MasterStats.Db.Marshaller;

import com.google.gson.reflect.TypeToken;
import com.lvack.MasterStats.Db.DataClasses.ChampionMasteryItem;

import java.util.List;

/**
 * ChampionMasteryItemListMarshallerClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * marshaller for List<ChampionMasteryItem> using the GsonMarshaller but overwriting
 * unmarshall to use a type toke instead of the passed class
 */
public class ChampionMasteryItemListMarshaller extends GsonMarshaller<List<ChampionMasteryItem>> {
    @Override
    public List<ChampionMasteryItem> unmarshall(Class<List<ChampionMasteryItem>> aClass, String json) {
        return gson.fromJson(json, new TypeToken<List<ChampionMasteryItem>>() {
        }.getType());
    }
}
