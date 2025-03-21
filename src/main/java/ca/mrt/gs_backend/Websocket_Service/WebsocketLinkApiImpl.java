package ca.mrt.gs_backend.Websocket_Service;


import ca.mrt.WebsocketService.api.AbstractWebsocketLinkApi;
import ca.mrt.WebsocketService.api.ParameterValue;
import ca.mrt.WebsocketService.api.SubscribeRequest;
import ca.mrt.WebsocketService.api.SubscribeResponse;
import org.yamcs.api.Observer;

import java.util.List;

public class WebsocketLinkApiImpl extends AbstractWebsocketLinkApi {

    private String getParameterValue(String instance, String processor, String paramName) {
        return "test";
    }


    @Override
    public void subscribe(Object ctx, SubscribeRequest request, Observer observer) {

        String instance = request.getInstance();
        String processor = request.getProcessor();
        List<String> parameterNames = request.getParameterNamesList();

        try {
            for (String paramName : parameterNames) {

                String paramValue = getParameterValue(instance, processor, paramName);


                ParameterValue parameterValue = ParameterValue.newBuilder()
                        .setName(paramName)
                        .setValue(paramValue)
                        .setGenerationTime(System.currentTimeMillis())
                        .setReceptionTime(System.currentTimeMillis())
                        .build();


                SubscribeResponse response = SubscribeResponse.newBuilder()
                        .addParameterValues(parameterValue)
                        .build();


                observer.next(response);
            }
            observer.complete();
        } catch (Exception e) {
            observer.completeExceptionally(e);
        }
    }
}
