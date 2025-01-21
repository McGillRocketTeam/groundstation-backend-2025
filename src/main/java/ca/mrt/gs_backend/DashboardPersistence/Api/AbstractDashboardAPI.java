package ca.mrt.gs_backend.DashboardPersistence.Api;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.yamcs.api.Api;
import org.yamcs.api.Observer;

public abstract class AbstractDashboardAPI implements Api {

    private final Descriptors.ServiceDescriptor serviceDescriptor;

    public AbstractDashboardAPI(Descriptors.ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

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
