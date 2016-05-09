package com.lvack.MasterStats.Db.Marshaller;

import com.google.gson.reflect.TypeToken;
import com.lvack.MasterStats.Db.DataClasses.ChampionMasteryItem;
import com.lvack.MasterStats.Db.DataClasses.SummonerItem;
import com.lvack.MasterStats.Util.Pair;

import java.util.List;

/**
 * SummonerItemChampionMasteryItemPairListMarshallerClass for MasterStats
 *
 * @author Leon Vack
 */

public class SummonerItemChampionMasteryItemPairListMarshaller extends GsonMarshaller<List<Pair<SummonerItem, ChampionMasteryItem>>> {
    @Override
    public String marshall(List<Pair<SummonerItem, ChampionMasteryItem>> map) {
        return gson.toJson(map, new TypeToken<List<Pair<SummonerItem, ChampionMasteryItem>>>() {}.getType());
    }

    @Override
    public List<Pair<SummonerItem, ChampionMasteryItem>> unmarshall(Class<List<Pair<SummonerItem, ChampionMasteryItem>>> aClass, String json) {
        return gson.fromJson(json, new TypeToken<List<Pair<SummonerItem, ChampionMasteryItem>>>() {}.getType());
    }
}
