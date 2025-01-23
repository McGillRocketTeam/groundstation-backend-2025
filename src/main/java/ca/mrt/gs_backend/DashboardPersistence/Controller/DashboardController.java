package ca.mrt.gs_backend.DashboardPersistence.Controller;

import ca.mrt.gs_backend.DashboardPersistence.Services.DashboardService;
import org.yamcs.logging.Log;

import java.util.HashMap;
import java.util.List;

public class DashboardController {

    DashboardService dashboardService = new DashboardService();
    private static final Log log = new Log(DashboardController.class);

    public void saveDashboard(HashMap<String, Object> dashboard) {dashboardService.saveDashboard(dashboardService.createDashboard(dashboard));}

    public void deleteDashboard(String path) {
        dashboardService.deleteDashboard(path);
    }

    public void updateDashboard(String oldPath, HashMap<String, Object> dashboard) {dashboardService.updateDashboard(oldPath, dashboard);}

    public String getAllDashboards() {return dashboardService.getArrayAsJson(dashboardService.getAllDashboards());}

    public String getDashboard(String path) {return dashboardService.getAsJson(dashboardService.getDashboard(path));}

    public void init(){
        dashboardService.init();
    }



}
