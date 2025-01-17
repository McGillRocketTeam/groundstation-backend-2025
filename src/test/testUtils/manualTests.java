package testUtils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Test;

public class manualTests {

    @Test
    public void testConnection(){
        MongoClient mongoClient = MongoClients.create("mongodb://root:root@localhost:27017/?authSource=admin");
        MongoDatabase database = mongoClient.getDatabase("testDatabase");
        database.createCollection("testCollection");
        MongoCollection<Document> testCollection = database.getCollection("testCollection");
        testCollection.insertOne(new Document("name", "testCollection"));
    }

}
