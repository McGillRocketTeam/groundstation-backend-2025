package ca.mrt.gs_backend.DashboardPersistence.Api;


import ca.mrt.gs_backend.DashboardPersistence.Api.generated.DeleteDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.GetAllDashboardsRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.SaveDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.UpdateDashboardRequest;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.yamcs.api.Api;
import org.yamcs.api.HttpBody;
import org.yamcs.api.Observer;

public abstract class AbstractDashboardAPI<Context> implements Api<Context> {

    private final Descriptors.ServiceDescriptor serviceDescriptor;

    public AbstractDashboardAPI(Descriptors.ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

    public abstract void getAllDashboards(GetAllDashboardsRequest request, Observer<HttpBody> observer);
    public abstract void saveDashboard(SaveDashboardRequest request, Observer<HttpBody> observer);
    public abstract void updateDashboard(UpdateDashboardRequest request, Observer<HttpBody> observer);
    public abstract void deleteDashboard(DeleteDashboardRequest request, Observer<HttpBody> observer);

    @Override
    public Descriptors.ServiceDescriptor getDescriptorForType() {
        return serviceDescriptor;
    }

    @Override
    public Message getRequestPrototype(Descriptors.MethodDescriptor methodDescriptor) {
        return null;
    }

    @Override
    public Message getResponsePrototype(Descriptors.MethodDescriptor methodDescriptor) {
        return null;
    }

    @Override
    public Observer<Message> callMethod(Descriptors.MethodDescriptor methodDescriptor, Object o, Observer observer) {
        return null;
    }

    @Override
    public void callMethod(Descriptors.MethodDescriptor methodDescriptor, Object o, Message message, Observer observer) {

    }
}
