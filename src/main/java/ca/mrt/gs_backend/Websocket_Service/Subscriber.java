package ca.mrt.gs_backend.Websocket_Service;

import ca.mrt.WebsocketService.api.SubscribeResponse;
import org.yamcs.api.Observer;

public class Subscriber {
    final Object ctx;
    final Observer<SubscribeResponse> observer;
    final String linkName;

    Subscriber(Object ctx, Observer<SubscribeResponse> observer, String linkName) {
        this.ctx = ctx;
        this.observer = observer;
        this.linkName = linkName;
    }
}
