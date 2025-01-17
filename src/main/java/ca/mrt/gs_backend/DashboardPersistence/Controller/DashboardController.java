package ca.mrt.gs_backend.DashboardPersistence.Controller;

import ca.mrt.gs_backend.DashboardPersistence.Services.DashboardService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.LoggerFactory;
import org.yamcs.InitException;
import org.yamcs.YamcsService;
import org.yamcs.http.AbstractHttpService;
import org.yamcs.http.HandlerContext;
import org.yamcs.http.HttpHandler;
import org.yamcs.http.HttpServer;

import java.net.http.HttpResponse;
import java.security.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class DashboardController extends AbstractHttpService implements YamcsService {

    DashboardService dashboardService = new DashboardService();

    public void saveDashboard(HashMap<String, Object> dashboard) {dashboardService.saveDashboard(dashboardService.createDashboard(dashboard));}

    public void deleteDashboard(String path) {
        dashboardService.deleteDashboard(path);
    }

    public void updateDashboard(String oldPath, HashMap<String, Object> dashboard) {dashboardService.updateDashboard(oldPath, dashboard);}

    public List<HashMap<String, Object>> getAllDashboards() {return dashboardService.getAllDashboards();}

    public HashMap<String, Object> getDashboard(String path) {return dashboardService.getDashboard(path);}
    @Override
    public void init(HttpServer httpServer) throws InitException {
        dashboardService = new DashboardService();
        dashboardService.init();
        log.info("Dashboard service initialized!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        httpServer.addRoute("/api/dashboards/", ()->new HttpHandler() {
                @Override
                public boolean requireAuth() {
                    return false;
                }

                @Override
                public void handle(HandlerContext ctx) {
                    ctx.requireGET();
                    List<HashMap<String, Object>> dashboards = dashboardService.getAllDashboards();
                    Gson gson = new Gson();
                    try {
                        String jsonResponse = gson.toJson(dashboards);
                        System.out.println(jsonResponse);
                        ctx.sendOK(new JsonParser().parse(jsonResponse).getAsJsonObject());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {

    }

    @Override
    public String getYamcsInstance() {
        return "";
    }
}
