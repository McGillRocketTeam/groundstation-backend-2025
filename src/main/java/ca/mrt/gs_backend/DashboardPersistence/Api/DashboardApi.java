package ca.mrt.gs_backend.DashboardPersistence.Api;


import ca.mrt.gs_backend.DashboardPersistence.Api.generated.DeleteDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.GetAllDashboardsRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.SaveDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.UpdateDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Controller.DashboardController;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import org.yamcs.api.HttpBody;
import org.yamcs.api.Observer;
import org.yamcs.http.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    }

    @Override
    public void updateDashboard(UpdateDashboardRequest request, Observer<HttpBody> observer) {

    }

    @Override
    public void deleteDashboard(DeleteDashboardRequest request, Observer<HttpBody> observer) {

    }
}
