package ca.mrt.gs_backend.DashboardPersistence.Api;


import ca.mrt.gs_backend.DashboardPersistence.Api.generated.*;
import ca.mrt.gs_backend.DashboardPersistence.Controller.DashboardController;
import com.google.protobuf.ByteString;
import org.yamcs.api.HttpBody;
import org.yamcs.api.Observer;
import org.yamcs.http.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DashboardApi extends AbstractDashboardAPI<Context> {
    DashboardController controller;

    public DashboardApi() {
        this.controller = new DashboardController();
        controller.init();
    }

    @Override
    public void getAllDashboards(GetAllDashboardsRequest request, Observer<HttpBody> observer) {
        String dashboards = controller.getAllDashboards();
        ByteString data = ByteString.copyFromUtf8(dashboards);
        HttpBody response = HttpBody.newBuilder()
                .setContentType("application/json")
                .setData(data)
                .build();
        observer.complete(response);
    }

    @Override
    public void saveDashboard(SaveDashboardRequest request, Observer<HttpBody> observer) {
        Dashboard dashboard = request.getDashboard();
        controller.saveDashboard(controller.dashboardService.getDashboardAsMap(DashboardFromProto(dashboard)));
        String dashboards = controller.getAllDashboards();
        ByteString data = ByteString.copyFromUtf8(dashboards);
        HttpBody response = HttpBody.newBuilder()
                .setContentType("application/json")
                .setData(data)
                .build();
        observer.complete(response);
    }

    @Override
    public void updateDashboard(UpdateDashboardRequest request, Observer<HttpBody> observer) {
        String oldPath = request.getOldPath();
        Dashboard dashboard = request.getDashboard();
        controller.updateDashboard(oldPath, controller.dashboardService.getDashboardAsMap(DashboardFromProto(dashboard)));
        String dashboards = controller.getAllDashboards();
        ByteString data = ByteString.copyFromUtf8(dashboards);
        HttpBody response = HttpBody.newBuilder()
                .setContentType("application/json")
                .setData(data)
                .build();
        observer.complete(response);
    }

    @Override
    public void deleteDashboard(DeleteDashboardRequest request, Observer<HttpBody> observer) {
        controller.deleteDashboard(request.getPath());
        String dashboards = controller.getAllDashboards();
        ByteString data = ByteString.copyFromUtf8(dashboards);
        HttpBody response = HttpBody.newBuilder()
                .setContentType("application/json")
                .setData(data)
                .build();
        observer.complete(response);
    }

    public ca.mrt.gs_backend.DashboardPersistence.Models.Dashboard DashboardFromProto(Dashboard dashboard) {
        ca.mrt.gs_backend.DashboardPersistence.Models.Dashboard dashboardModel = new ca.mrt.gs_backend.DashboardPersistence.Models.Dashboard();
        dashboardModel.setName(dashboard.getName());
        dashboardModel.setIconName(dashboard.getIconName());
        dashboardModel.setPath(dashboard.getPath());
        List<ca.mrt.gs_backend.DashboardPersistence.Models.Card> cardList = dashboard.getCardsList().stream().map(card -> CardFromProto(card)).toList();
        return dashboardModel;
    }

    public ca.mrt.gs_backend.DashboardPersistence.Models.Card CardFromProto(Card card) {
        ca.mrt.gs_backend.DashboardPersistence.Models.Card cardModel = new ca.mrt.gs_backend.DashboardPersistence.Models.Card();
        cardModel.setIndex(card.getIndex());
        cardModel.setHeight(card.getHeight());
        cardModel.setWidth(card.getWidth());
        cardModel.setY(card.getY());
        cardModel.setX(card.getX());
        cardModel.setConfig(card.getConfig());
        return cardModel;
    }
}
