package ca.mrt.gs_backend.DashboardPersistence.Api;


import ca.mrt.gs_backend.DashboardPersistence.Api.generated.DeleteDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.GetAllDashboardsRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.SaveDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.UpdateDashboardRequest;
import org.yamcs.api.HttpBody;
import org.yamcs.api.Observer;
import org.yamcs.http.Context;

public class DashboardApi implements AbstractDashboardAPI<Context>{
    @Override
    public void getAllDashboards(GetAllDashboardsRequest request, Observer<HttpBody> observer) {

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
