package com.lvack.MasterStats.Db.Marshaller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.google.gson.Gson;
import com.lvack.MasterStats.Util.GsonProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * GsonMarshallerClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * generic dynamo db marshaller using gson to marshall and unmarshall objects
 *
 * @param <K> type of objects to be marshalled
 */
@Slf4j
public class GsonMarshaller<K> implements DynamoDBMarshaller<K> {
    protected final Gson gson = GsonProvider.getGSON();

    @Override
    public String marshall(K map) {
        return gson.toJson(map);
    }

    @Override
    public K unmarshall(Class<K> aClass, String json) {
        return gson.fromJson(json, aClass);
    }
}
