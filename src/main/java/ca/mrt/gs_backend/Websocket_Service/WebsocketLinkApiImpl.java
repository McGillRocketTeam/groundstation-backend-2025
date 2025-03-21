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
        Subscriber subscriber = new Subscriber(ctx, observer, request.getParameterNamesList());
        subscribers.add(subscriber);
        sendInitialParameterValues(subscriber);
    }

    private void sendInitialParameterValues(Subscriber subscriber) {
        // Retrieve and send initial parameter values to the subscriber
        for (String paramName : subscriber.parameterNames) {
            String paramValue = getParameterValue(subscriber.ctx, paramName);
            ParameterValue parameterValue = ParameterValue.newBuilder()
                    .setName(paramName)
                    .setValue(paramValue)
                    .setGenerationTime(System.currentTimeMillis())
                    .setReceptionTime(System.currentTimeMillis())
                    .build();
            SubscribeResponse response = SubscribeResponse.newBuilder()
                    .addParameterValues(parameterValue)
                    .build();
            subscriber.observer.next(response);
        }
    }

    public void onParameterUpdate(String paramName, String newValue) {
        long currentTime = System.currentTimeMillis();
        ParameterValue parameterValue = ParameterValue.newBuilder()
                .setName(paramName)
                .setValue(newValue)
                .setGenerationTime(currentTime)
                .setReceptionTime(currentTime)
                .build();
        SubscribeResponse response = SubscribeResponse.newBuilder()
                .addParameterValues(parameterValue)
                .build();


        for (Subscriber subscriber : subscribers) {
            if (subscriber.parameterNames.contains(paramName)) {
                subscriber.observer.next(response);
            }
        }
    }
}
