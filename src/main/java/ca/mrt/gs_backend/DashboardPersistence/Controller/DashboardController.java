package ca.mrt.gs_backend.DashboardPersistence.Controller;

import ca.mrt.gs_backend.DashboardPersistence.Services.DashboardService;

import java.util.HashMap;

public class DashboardController {
    DashboardService dashboardService = new DashboardService();

    public void saveDashboard(HashMap<String, Object> dashboard) {
        dashboardService.saveDashboard(dashboardService.createDashboard(dashboard));
    }

    public void deleteDashboard(String path) {
        dashboardService.deleteDashboard(path);
    }

    public void updateDashboard(String oldPath, HashMap<String, Object> dashboard) {
        dashboardService.updateDashboard(oldPath, dashboard);
    }
}
