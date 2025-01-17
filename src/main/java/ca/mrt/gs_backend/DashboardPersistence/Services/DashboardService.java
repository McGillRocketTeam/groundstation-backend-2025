package ca.mrt.gs_backend.DashboardPersistence.Services;

import ca.mrt.gs_backend.DashboardPersistence.Models.Dashboard;
import ca.mrt.gs_backend.DashboardPersistence.MongoHandler;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class DashboardService {

    MongoHandler mongoHandler = new MongoHandler();

    public Dashboard createDashboard(HashMap<String, Object> map) {
        Dashboard dashboard = new Dashboard();
        CardService cardService = new CardService();
        dashboard.setName((String) map.get("name"));
        dashboard.setPath((String) map.get("path"));
        dashboard.setIconName((String) map.get("icon_name"));

        for (HashMap<String, Object> card : (List<HashMap<String,Object>>)map.get("layout")) {
            dashboard.getLayout().add(cardService.createCardFromMap(card));
        }
        return dashboard;
    }

    public HashMap<String, Object> getDashboardAsMap(Dashboard dashboard) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", dashboard.getName());
        map.put("path", dashboard.getPath());
        map.put("icon_name", dashboard.getIconName());
        List<HashMap<String, Object>> layout = dashboard.getLayout().stream().
                map(card ->new CardService().getCardAsMap(card))
                .toList();
        map.put("layout", layout);
        return map;
    }


    public void saveDashboard(Dashboard dashboard) {
        Document dashboardDocument = mongoHandler.createDocument(getDashboardAsMap(dashboard));
        mongoHandler.addDocumentToCollection(mongoHandler.databaseName, mongoHandler.collectionName, dashboardDocument);
    }

    public void deleteDashboard(String path) {
        MongoCollection collection = mongoHandler.getCollection(mongoHandler.collectionName);
        collection.deleteOne(eq("path", path));
    }

    public void updateDashboard(String oldPath, HashMap<String, Object> newMap) {
        Dashboard newDashboard = createDashboard(newMap);
        MongoCollection collection = mongoHandler.getCollection(mongoHandler.collectionName);
        collection.updateOne(eq("path", oldPath),combine(
                set("name",newDashboard.getName()),
                set("path",newDashboard.getPath()),
                set("icon_name",newDashboard.getIconName()),
                set("layout",newDashboard.getLayout()
                )));
    }


}
