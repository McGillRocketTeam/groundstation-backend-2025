package ca.mrt.gs_backend.Websocket_Service;


import ca.mrt.WebsocketService.api.AbstractWebsocketLinkApi;
import ca.mrt.WebsocketService.api.ParameterValue;
import ca.mrt.WebsocketService.api.SubscribeRequest;
import ca.mrt.WebsocketService.api.SubscribeResponse;
import org.yamcs.api.Observer;

import java.util.ArrayList;
import java.util.List;

public class WebsocketLinkApiImpl extends AbstractWebsocketLinkApi {
    WebsocketController controller;

    public WebsocketLinkApiImpl(WebsocketController websocketController) {
        websocketController.api = this;
        controller = websocketController;
    }

    private String getParameterValue(Object ctx, String paramName) {
        return controller.getParamValue(":D");
    }

    private List<Subscriber> subscribers = new ArrayList<>();


    @Override
    public void subscribe(Object ctx, SubscribeRequest request, Observer observer) {
        Subscriber subscriber = new Subscriber(ctx, observer, request.getLinkName());
        subscribers.add(subscriber);
        sendInitialParameterValues(subscriber);
    }

    private void sendInitialParameterValues(Subscriber subscriber) {
        // Retrieve and send initial parameter values to the subscriber
            String linkName = subscriber.linkName;
            String paramValue = getParameterValue(subscriber.ctx, linkName);
            ParameterValue parameterValue = ParameterValue.newBuilder()
                    .setName(linkName)
                    .setValue(paramValue)
                    .setGenerationTime(System.currentTimeMillis())
                    .setReceptionTime(System.currentTimeMillis())
                    .build();
            SubscribeResponse response = SubscribeResponse.newBuilder()
                    .addParameterValues(parameterValue)
                    .build();
            subscriber.observer.next(response);
    }

    public void onParameterUpdate(String linkName, String newValue) {
        long currentTime = System.currentTimeMillis();
        ParameterValue parameterValue = ParameterValue.newBuilder()
                .setName(linkName)
                .setValue(newValue)
                .setGenerationTime(currentTime)
                .setReceptionTime(currentTime)
                .build();
        SubscribeResponse response = SubscribeResponse.newBuilder()
                .addParameterValues(parameterValue)
                .build();


        for (Subscriber subscriber : subscribers) {
            if (subscriber.linkName.equals(linkName)) {
                subscriber.observer.next(response);
            }
        }
    }
}
