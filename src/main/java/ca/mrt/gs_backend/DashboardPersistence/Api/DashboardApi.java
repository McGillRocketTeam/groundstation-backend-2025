package ca.mrt.gs_backend.DashboardPersistence.Api;


import ca.mrt.gs_backend.DashboardPersistence.Api.generated.DeleteDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.GetAllDashboardsRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.SaveDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.UpdateDashboardRequest;
import com.google.protobuf.Descriptors;
import org.yamcs.api.HttpBody;
import org.yamcs.api.Observer;
import org.yamcs.http.Context;

public class DashboardApi extends AbstractDashboardAPI<Context> {
    public DashboardApi(Descriptors.ServiceDescriptor serviceDescriptor) {
        super(serviceDescriptor);
    }

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
