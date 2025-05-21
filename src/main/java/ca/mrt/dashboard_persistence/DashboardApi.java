package ca.mrt.dashboard_persistence;

import ca.mrt.dashboard_persistence.api.*;
import ca.mrt.dashboard_persistence.controller.DashboardController;
import ca.mrt.dashboard_persistence.models.Card;
import ca.mrt.dashboard_persistence.models.Dashboard;
import ca.mrt.dashboard_persistence.services.DashboardService;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import org.yamcs.api.HttpBody;
import org.yamcs.api.Observer;
import org.yamcs.http.Context;
import org.yamcs.http.ForbiddenException;
import org.yamcs.http.NotFoundException;

import java.util.List;

public class DashboardApi extends AbstractDashboardApi<Context> {
    DashboardController controller;

    public DashboardApi() {
        this.controller = new DashboardController();
        controller.init();
    }

    @Override
    public void getAllDashboards(Context ctx, Empty request, Observer<HttpBody> observer) {
        String dashboards = controller.getAllDashboards();
        ByteString data = ByteString.copyFromUtf8(dashboards);
        HttpBody response = HttpBody.newBuilder()
                .setContentType("application/json")
                .setData(data)
                .build();
        observer.complete(response);
    }

    @Override
    public void saveDashboard(Context ctx, SaveDashboardRequest request, Observer<Empty> observer) {
        ca.mrt.dashboard_persistence.api.Dashboard dashboard = request.getDashboard();

        if(controller.saveDashboard(DashboardService.getDashboardAsMap(DashboardFromProto(dashboard)))){
            observer.complete(Empty.getDefaultInstance());
        } else {
            observer.completeExceptionally(new ForbiddenException("Dashboard with path already exists"));
        }

    }

    @Override
    public void updateDashboard(Context ctx, UpdateDashboardRequest request, Observer<HttpBody> observer) {
        String oldPath = request.getOldPath();
        ca.mrt.dashboard_persistence.api.Dashboard dashboard = request.getDashboard();
        controller.updateDashboard(oldPath, DashboardService.getDashboardAsMap(DashboardFromProto(dashboard)));
        String dashboards = controller.getAllDashboards();
        ByteString data = ByteString.copyFromUtf8(dashboards);
        HttpBody response = HttpBody.newBuilder()
                .setContentType("application/json")
                .setData(data)
                .build();
        observer.complete(response);
    }

    @Override
    public void getDashboard(Context ctx, GetDashboardRequest request, Observer<HttpBody> observer) {
        String path = request.getDashboardPath();
        var strOpt = controller.getDashboard(path);


        if(strOpt.isPresent()){
            ByteString data = ByteString.copyFromUtf8(strOpt.get());
            HttpBody response = HttpBody.newBuilder()
                    .setContentType("application/json")
                    .setData(data)
                    .build();
            observer.complete(response);
        } else {
            observer.completeExceptionally(new NotFoundException("No dashboard with path exists"));
        }
    }

    @Override
    public void deleteDashboard(Context ctx, GetDashboardRequest request, Observer<Empty> observer) {
        if(controller.deleteDashboard(request.getDashboardPath())){
            observer.complete(Empty.getDefaultInstance());
        } else {
            observer.completeExceptionally(new NotFoundException("No dashboard with path exists"));
        }

    }

    public Dashboard DashboardFromProto(ca.mrt.dashboard_persistence.api.Dashboard dashboard) {
        Dashboard dashboardModel = new Dashboard();
        dashboardModel.setName(dashboard.getName());
        dashboardModel.setIconName(dashboard.getIconName());
        dashboardModel.setPath(dashboard.getPath());
        List<Card> cardList = dashboard.getLayoutList().stream().map(this::CardFromProto).toList();
        dashboardModel.setLayout(cardList);
        return dashboardModel;
    }

    public Card CardFromProto(ca.mrt.dashboard_persistence.api.Card card) {
        Card cardModel = new Card();
        cardModel.setIndex(card.getI());
        cardModel.setHeight(card.getH());
        cardModel.setWidth(card.getW());
        cardModel.setY(card.getY());
        cardModel.setX(card.getX());
        cardModel.setConfig(card.getConfig());
        return cardModel;
    }
}
