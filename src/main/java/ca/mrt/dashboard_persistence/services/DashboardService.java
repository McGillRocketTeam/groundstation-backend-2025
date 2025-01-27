package ca.mrt.dashboard_persistence.services;

import ca.mrt.dashboard_persistence.MongoHandler;
import ca.mrt.dashboard_persistence.models.Card;
import ca.mrt.dashboard_persistence.models.Dashboard;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public String getAsJson(HashMap<String, Object> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    public String getArrayAsJson(List<HashMap<String, Object>> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
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

    public HashMap<String, Object> getDashboard(String path) {
        MongoCollection collection = mongoHandler.getCollection(mongoHandler.collectionName);
        CardService cardService = new CardService();
        Document dashboardDoc = (Document) collection.find(eq("path", path)).first();
        Dashboard dashboard = new Dashboard();
        dashboard.setName((String) dashboardDoc.get("name"));
        dashboard.setPath((String) dashboardDoc.get("path"));
        dashboard.setIconName((String) dashboardDoc.get("icon_name"));
        List<Document> layout = (List<Document>) dashboardDoc.get("layout");
        dashboard.setLayout(layout.stream().map(card ->{
            Card cardInLayout = new Card();
            cardInLayout.setY((Integer) card.get("y"));
            cardInLayout.setX((Integer) card.get("x"));
            cardInLayout.setHeight((Integer) card.get("w"));
            cardInLayout.setWidth((Integer) card.get("h"));
            cardInLayout.setIndex(card.get("y").toString());
            cardInLayout.setConfig(card.get("config").toString());
            return cardInLayout;
        }).toList());
        return getDashboardAsMap(dashboard);
    }

    public List<HashMap<String, Object>> getAllDashboards() {
        MongoCollection collection = mongoHandler.getCollection(mongoHandler.collectionName);
        MongoIterable<Document> iterable = collection.find(new Document());
        List<HashMap<String, Object>> dashboards = new ArrayList<>();
        for (Document document : iterable) {
            HashMap<String, Object> dashboard = getDashboard(document.getString("path"));
            dashboards.add(dashboard);
        }
        return dashboards;
    }

    public void init() {
        this.mongoHandler = new MongoHandler();
        mongoHandler.getDatabase(mongoHandler.databaseName);
    }

}
