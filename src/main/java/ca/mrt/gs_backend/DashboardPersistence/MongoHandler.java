package ca.mrt.gs_backend.DashboardPersistence;


import com.mongodb.Mongo;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.HashMap;

public class MongoHandler {
     MongoClient mongoClient = null;
     public String databaseName = "yamcsV2";
     public String collectionName = "dashboards";


    public MongoDatabase getDatabase(String db) {
        if (mongoClient == null )
        {
            mongoClient = MongoClients.create("mongodb://root:root@localhost:27017/admin");
        }
        MongoDatabase database = mongoClient.getDatabase(db);
        return database;
    }

    /**
     * @param database Database to drop
     * @author Tarek Namani
     * Drops the database, removing all the collectinos and documents inside of it
     */
    public void dropDatabase(MongoDatabase database) {
        database.drop();
    }

    public void addDocumentToCollection(String db, String collection, Document document) {
        MongoDatabase database = getDatabase(db);
        database.getCollection(collection).insertOne(document);

    }

    public Document createDocument(HashMap<String, Object> map) {
        return new Document(map);
    }

    public MongoCollection getCollection(String collection) {
        MongoDatabase database = getDatabase(databaseName);
        return database.getCollection(collection);
    }

}
