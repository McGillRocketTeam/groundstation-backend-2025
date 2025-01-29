package ca.mrt.dashboard_persistence.controller;

import ca.mrt.dashboard_persistence.services.DashboardService;
import org.yamcs.logging.Log;

import java.util.Map;
import java.util.Optional;

public class DashboardController {

    private final DashboardService dashboardService = new DashboardService();
    private static final Log log = new Log(DashboardController.class);

    public boolean saveDashboard(Map<String, Object> dashboard) {
        return dashboardService.saveDashboard(dashboardService.createDashboard(dashboard));
    }

    public boolean deleteDashboard(String path) {
        return dashboardService.deleteDashboard(path);
    }

    public void updateDashboard(String oldPath, Map<String, Object> dashboard) {dashboardService.updateDashboard(oldPath, dashboard);}

    public String getAllDashboards() {return dashboardService.getArrayAsJson(dashboardService.getAllDashboards());}

    public Optional<String> getDashboard(String path) {
        var dashboardOpt = dashboardService.getDashboard(path);
        return dashboardOpt.map(dashboardService::getAsJson);
    }

    public void init(){
        dashboardService.init();
    }

}
