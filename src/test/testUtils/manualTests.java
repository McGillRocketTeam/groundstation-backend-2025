package testUtils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.junit.Test;

public class manualTests {

    @Test
    public void testConnection(){
        MongoClient mongoClient = MongoClients.create("mongodb://root:example@mongodb:27017");
        MongoDatabase database = mongoClient.getDatabase("testDatabase");
    }

}
