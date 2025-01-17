package testUtils;

import ca.mrt.gs_backend.DashboardPersistence.MongoHandler;
import com.mongodb.Block;
import com.mongodb.client.*;
import org.bson.Document;
import org.junit.Test;

import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;

public class manualTests {

    @Test
    public void testConnection(){
        MongoHandler handler = new MongoHandler();
        HashMap<String, Object> map = new HashMap<>();
        map.put("funny", "yes");
        map.put("intersting", "no");
        map.put("maybe", 2);
        HashMap<String, Object> innerMap = new HashMap<>();
        innerMap.put("funny", "yes");
        map.put("inception", innerMap);
        Document newDoc = handler.createDocument(map);
        handler.addDocumentToCollection("testDatabase","yessir", newDoc);
        MongoDatabase db = handler.getDatabase("testDatabase");
        MongoIterable<String> collections = db.listCollectionNames();
        for (String collection : collections) {
            FindIterable<Document> findIterable = db.getCollection(collection).find(new Document());
            findIterable.forEach((Block<? super Document>) doc -> System.out.println(doc));
        }


    }

}
