package ca.mrt.gs_backend.DashboardPersistence.Controller;

import ca.mrt.gs_backend.DashboardPersistence.Api.AbstractDashboardAPI;
import ca.mrt.gs_backend.DashboardPersistence.Services.DashboardService;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.yamcs.*;
import org.yamcs.api.Api;
import org.yamcs.api.HttpBody;
import org.yamcs.api.Observer;
import org.yamcs.http.HttpServer;
import org.yamcs.logging.Log;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.Provider;
import java.util.HashMap;
import java.util.List;

import static org.openjdk.nashorn.internal.runtime.regexp.joni.Config.log;

public class DashboardController {

    DashboardService dashboardService = new DashboardService();
    private static final Log log = new Log(DashboardController.class);

    public void saveDashboard(HashMap<String, Object> dashboard) {dashboardService.saveDashboard(dashboardService.createDashboard(dashboard));}

    public void deleteDashboard(String path) {
        dashboardService.deleteDashboard(path);
    }

    public void updateDashboard(String oldPath, HashMap<String, Object> dashboard) {dashboardService.updateDashboard(oldPath, dashboard);}

    public List<HashMap<String, Object>> getAllDashboards() {return dashboardService.getAllDashboards();}

    public HashMap<String, Object> getDashboard(String path) {return dashboardService.getDashboard(path);}



}
