package ca.mrt.gs_backend.cardPersistence;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class CardRepository {
    String connection = "mongodb://root:root@localhost:27017/?authSource=admin";
    MongoDatabase database;
    MongoClient client;


    public MongoCollection<Document> createCollection(String collectionName) {
        database.createCollection(collectionName);
        return database.getCollection(collectionName);

    }

    public void init() {
        MongoClient mongoClient = MongoClients.create("mongodb://root:root@localhost:27017/?authSource=admin");
    }
}
