package ca.mrt.dashboard_persistence;

import ca.mrt.dashboard_persistence.api.*;
import ca.mrt.dashboard_persistence.controller.DashboardController;
import ca.mrt.dashboard_persistence.models.Card;
import ca.mrt.dashboard_persistence.models.Dashboard;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import org.yamcs.api.HttpBody;
import org.yamcs.api.Observer;
import org.yamcs.http.Context;

import java.util.List;

/**
 * Responds to HTTP requests with current metrics in any of the Prometheus exposition formats.
 */
public class DashboardApi extends AbstractDashboardApi<Context> {
    DashboardController controller;

    public DashboardApi() {
        this.controller = new DashboardController();
        controller.init();
    }

    @Override
    public void getAllDashboards(Context ctx, GetAllDashboardsRequest request, Observer<HttpBody> observer) {
        String dashboards = controller.getAllDashboards();
        ByteString data = ByteString.copyFromUtf8(dashboards);
        HttpBody response = HttpBody.newBuilder()
                .setContentType("application/json")
                .setData(data)
                .build();
        observer.complete(response);
    }

    @Override
    public void saveDashboard(Context ctx, SaveDashboardRequest request, Observer<HttpBody> observer) {
        ca.mrt.dashboard_persistence.api.Dashboard dashboard = request.getDashboard();
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
    public void updateDashboard(Context ctx, UpdateDashboardRequest request, Observer<HttpBody> observer) {
        String oldPath = request.getOldPath();
        ca.mrt.dashboard_persistence.api.Dashboard dashboard = request.getDashboard();
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
    public void deleteDashboard(Context ctx, DeleteDashboardRequest request, Observer<Empty> observer) {
        controller.deleteDashboard(request.getPath());
        String dashboards = controller.getAllDashboards();
        ByteString data = ByteString.copyFromUtf8(dashboards);
        HttpBody response = HttpBody.newBuilder()
                .setContentType("application/json")
                .setData(data)
                .build();
        observer.complete(Empty.getDefaultInstance());
    }

    public Dashboard DashboardFromProto(ca.mrt.dashboard_persistence.api.Dashboard dashboard) {
        Dashboard dashboardModel = new Dashboard();
        dashboardModel.setName(dashboard.getName());
        dashboardModel.setIconName(dashboard.getIconName());
        dashboardModel.setPath(dashboard.getPath());
        List<Card> cardList = dashboard.getCardsList().stream().map(this::CardFromProto).toList();
        dashboardModel.setLayout(cardList);
        return dashboardModel;
    }

    public Card CardFromProto(ca.mrt.dashboard_persistence.api.Card card) {
        Card cardModel = new Card();
        cardModel.setIndex(card.getIndex());
        cardModel.setHeight(card.getHeight());
        cardModel.setWidth(card.getWidth());
        cardModel.setY(card.getY());
        cardModel.setX(card.getX());
        cardModel.setConfig(card.getConfig());
        return cardModel;
    }
}
