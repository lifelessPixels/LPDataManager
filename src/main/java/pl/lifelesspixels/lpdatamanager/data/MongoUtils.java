package pl.lifelesspixels.lpdatamanager.data;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.UUID;

public class MongoUtils {

    private static final String idFieldName = "playerID";

    public static <T extends DatabaseEntry> T getData(MongoDatabase database, String collectionName, UUID id, Class<T> dataClass) {
        MongoCollection<T> collection = database.getCollection(collectionName, dataClass);
        Bson filter = Filters.eq(idFieldName, id.toString());
        return collection.find(filter).first();
    }

    public static <T extends DatabaseEntry> void setData(MongoDatabase database, String collectionName, T data, Class<T> dataClass) {
        MongoCollection<T> collection = database.getCollection(collectionName, dataClass);
        Bson filter = Filters.eq(idFieldName, data.getPlayerID());
        if(collection.find(filter).first() == null)
            collection.insertOne(data);
        else collection.findOneAndReplace(filter, data);
    }

    public static void removeData(MongoDatabase database, String collectionName, UUID id) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Bson filter = Filters.eq(idFieldName, id.toString());
        collection.deleteOne(filter);
    }

}
