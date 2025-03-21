package ca.mrt.gs_backend.Websocket_Service;

public class WebsocketController {
    WebsocketLinkApiImpl api;

    //placeholder for now, eventually work on making a thread that looks for updates of the given param or link, if any updates are found then call the update from api
    public String getParamValue(String param) {
        return "test";

    }

}
