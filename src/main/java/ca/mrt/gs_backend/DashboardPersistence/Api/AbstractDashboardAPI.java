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
import org.yamcs.web.api.WebProto;

public abstract class AbstractDashboardAPI<Context> implements Api<Context> {

    public abstract void getAllDashboards(GetAllDashboardsRequest request, Observer<HttpBody> observer);
    public abstract void saveDashboard(SaveDashboardRequest request, Observer<HttpBody> observer);
    public abstract void updateDashboard(UpdateDashboardRequest request, Observer<HttpBody> observer);
    public abstract void deleteDashboard(DeleteDashboardRequest request, Observer<HttpBody> observer);

    @Override
    public Descriptors.ServiceDescriptor getDescriptorForType() {
        return WebProto.getDescriptor().getServices().get(0);
    }

    @Override
    public Message getRequestPrototype(Descriptors.MethodDescriptor methodDescriptor) {
        if (methodDescriptor.getService() != getDescriptorForType()) {
            throw new IllegalArgumentException("Method not contained by this service.");
        }
        return switch (methodDescriptor.getIndex()) {
            case 0 -> Empty.getDefaultInstance();
            case 1 -> Empty.getDefaultInstance();
            case 2 -> Empty.getDefaultInstance();
            case 3 -> Empty.getDefaultInstance();
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public Message getResponsePrototype(Descriptors.MethodDescriptor methodDescriptor) {
        if (methodDescriptor.getService() != getDescriptorForType()) {
            throw new IllegalArgumentException("Method not contained by this service.");
        }
        return switch (methodDescriptor.getIndex()) {
            case 0 -> GetAllDashboardsRequest.getDefaultInstance();
            case 1 -> SaveDashboardRequest.getDefaultInstance();
            case 2 -> UpdateDashboardRequest.getDefaultInstance();
            case 3 -> DeleteDashboardRequest.getDefaultInstance();
            default -> throw new IllegalStateException();
        };
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
