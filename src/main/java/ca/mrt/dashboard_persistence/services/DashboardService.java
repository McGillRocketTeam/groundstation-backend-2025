package ca.mrt.dashboard_persistence.services;

import ca.mrt.dashboard_persistence.MongoHandler;
import ca.mrt.dashboard_persistence.models.Card;
import ca.mrt.dashboard_persistence.models.Dashboard;
import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class DashboardService {

    MongoHandler mongoHandler = new MongoHandler();
    CardService cardService = new CardService();

    public Dashboard createDashboard(Map<String, Object> map) {
        Dashboard dashboard = new Dashboard();
        dashboard.setName((String) map.get("name"));
        dashboard.setPath((String) map.get("path"));
        dashboard.setIconName((String) map.get("icon_name"));

        for (Map<String, Object> card : (List<Map<String,Object>>)map.get("layout")) {
            dashboard.getLayout().add(cardService.createCardFromMap(card));
        }
        return dashboard;
    }

    public static Map<String, Object> getDashboardAsMap(Dashboard dashboard) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", dashboard.getName());
        map.put("path", dashboard.getPath());
        map.put("icon_name", dashboard.getIconName());
        List<Map<String, Object>> layout = dashboard.getLayout().stream().
                map(card ->new CardService().getCardAsMap(card))
                .toList();
        map.put("layout", layout);
        return map;
    }

    public String getAsJson(Map<String, Object> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    public String getArrayAsJson(List<Map<String, Object>> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }


    public boolean saveDashboard(Dashboard dashboard) {
        var existingDb = getDashboard(dashboard.getPath());
        if(existingDb.isPresent()){
            return false;
        }
        Document dashboardDocument = mongoHandler.createDocument(getDashboardAsMap(dashboard));
        mongoHandler.addDocumentToCollection(mongoHandler.databaseName, mongoHandler.collectionName, dashboardDocument);
        return true;
    }

    public boolean deleteDashboard(String path) {
        MongoCollection collection = mongoHandler.getCollection(mongoHandler.collectionName);
        DeleteResult result = collection.deleteMany(eq("path", path));
        return result.getDeletedCount() > 0L;
    }

    public void updateDashboard(String oldPath, Map<String, Object> newMap) {
        Dashboard newDashboard = createDashboard(newMap);
        MongoCollection collection = mongoHandler.getCollection(mongoHandler.collectionName);
        collection.updateOne(eq("path", oldPath),combine(
                set("name",newDashboard.getName()),
                set("path",newDashboard.getPath()),
                set("icon_name",newDashboard.getIconName()),
                set("layout",newDashboard.getLayout()
                )));
    }

    public Optional<Map<String, Object>> getDashboard(String path) {
        MongoCollection collection = mongoHandler.getCollection(mongoHandler.collectionName);
        Document dashboardDoc = (Document) collection.find(eq("path", path)).first();
        if(dashboardDoc == null){
            return Optional.empty();
        }
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
        return Optional.of(getDashboardAsMap(dashboard));
    }

    public List<Map<String, Object>> getAllDashboards() {
        MongoCollection collection = mongoHandler.getCollection(mongoHandler.collectionName);
        FindIterable<Document> iterable = collection.find(new Document());
        List<Map<String, Object>> dashboards = new ArrayList<>();
        for (Document document : iterable) {
            Map<String, Object> dashboard = document.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            dashboards.add(dashboard);
        }
        return dashboards;
    }

    public void init() {
        this.mongoHandler = new MongoHandler();
        mongoHandler.getDatabase(mongoHandler.databaseName);
    }

}
