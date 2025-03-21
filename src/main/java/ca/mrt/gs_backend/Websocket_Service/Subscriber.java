package ca.mrt.gs_backend.Websocket_Service;

import ca.mrt.WebsocketService.api.SubscribeResponse;
import org.yamcs.api.Observer;

import java.util.List;

public class Subscriber {
    final Object ctx;
    final Observer<SubscribeResponse> observer;
    final List<String> parameterNames;

    Subscriber(Object ctx, Observer<SubscribeResponse> observer, List<String> parameterNames) {
        this.ctx = ctx;
        this.observer = observer;
        this.parameterNames = parameterNames;
    }
}
