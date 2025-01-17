package testUtils;

import ca.mrt.gs_backend.DashboardPersistence.Controller.DashboardController;
import ca.mrt.gs_backend.DashboardPersistence.Models.Card;
import ca.mrt.gs_backend.DashboardPersistence.Models.Dashboard;
import ca.mrt.gs_backend.DashboardPersistence.MongoHandler;
import ca.mrt.gs_backend.DashboardPersistence.Services.DashboardService;
import com.mongodb.Block;
import com.mongodb.client.*;
import org.bson.Document;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;

public class manualTests {

    @Test
    public void testConnection(){
        MongoHandler handler = new MongoHandler();

        Card card = new Card();
        card.setY(1);
        card.setX(2);
        card.setWidth(3);
        card.setHeight(4);
        card.setIndex("5");


        HashMap<String, Object> map = new HashMap<>();
        map.put("funny", "yes");
        map.put("intersting", "no");
        map.put("maybe", 2);
        card.setConfig(map.toString());

        Dashboard dashboard = new Dashboard();
        dashboard.setLayout(Arrays.asList(card,card,card));
        dashboard.setPath("/new");
        dashboard.setName("Some name");
        dashboard.setIconName("fire");

        DashboardService service = new DashboardService();
        service.init();
        //service.getDashboard("/new");
        //service.deleteDashboard("/new");
        //service.saveDashboard(dashboard);

        MongoDatabase db = handler.getDatabase(handler.databaseName);
        MongoIterable<String> collections = db.listCollectionNames();
        for (String collection : collections) {
            FindIterable<Document> findIterable = db.getCollection(collection).find(new Document());
            //findIterable.forEach((Block<? super Document>) doc -> System.out.println(doc));
        }

        System.out.println(service.getAllDashboards());


    }

}
