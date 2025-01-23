package ca.mrt.gs_backend.DashboardPersistence.Api;


import ca.mrt.gs_backend.DashboardPersistence.Api.generated.DeleteDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.GetAllDashboardsRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.SaveDashboardRequest;
import ca.mrt.gs_backend.DashboardPersistence.Api.generated.UpdateDashboardRequest;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import org.yamcs.api.Api;
import org.yamcs.api.HttpBody;
import org.yamcs.api.Observer;
import org.yamcs.web.api.ParseFilterData;

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
        if (methodDescriptor.getService() != getDescriptorForType()) {
            throw new IllegalArgumentException("Method not contained by this service.");
        }
        switch (methodDescriptor.getIndex()) {
            case 0:
                return Empty.getDefaultInstance();
            case 1:
                return Empty.getDefaultInstance();
            case 2:
                return Empty.getDefaultInstance();
            case 3:
                return Empty.getDefaultInstance();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public Message getResponsePrototype(Descriptors.MethodDescriptor methodDescriptor) {
        if (methodDescriptor.getService() != getDescriptorForType()) {
            throw new IllegalArgumentException("Method not contained by this service.");
        }
        switch (methodDescriptor.getIndex()) {
            case 0:
                return GetAllDashboardsRequest.getDefaultInstance();
            case 1:
                return SaveDashboardRequest.getDefaultInstance();
            case 2:
                return UpdateDashboardRequest.getDefaultInstance();
            case 3:
                return DeleteDashboardRequest.getDefaultInstance();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public Observer<Message> callMethod(Descriptors.MethodDescriptor methodDescriptor, Object o, Observer observer) {
        if (methodDescriptor.getService() != getDescriptorForType()) {
            throw new IllegalArgumentException("Method not contained by this service.");
        }
        switch (methodDescriptor.getIndex()) {
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void callMethod(Descriptors.MethodDescriptor methodDescriptor, Object o, Message message, Observer observer) {
        if (methodDescriptor.getService() != getDescriptorForType()) {
            throw new IllegalArgumentException("Method not contained by this service.");
        }
        switch (methodDescriptor.getIndex()) {
            case 0:
                getAllDashboards((GetAllDashboardsRequest) message, observer);
                return ;
            case 1:
                saveDashboard((SaveDashboardRequest) message, observer);
                return ;
            case 2:
                updateDashboard((UpdateDashboardRequest) message, observer);
                return ;
            case 3:
                deleteDashboard((DeleteDashboardRequest) message, observer);
                return ;
            default:
                throw new IllegalStateException();
        }

    }
}
