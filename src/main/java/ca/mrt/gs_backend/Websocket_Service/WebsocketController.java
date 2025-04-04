package ca.mrt.gs_backend.Websocket_Service;

import ca.mrt.gs_backend.MyCommandPostprocessor;
import ca.mrt.gs_backend.MyPacketPreprocessor;

import java.util.ArrayList;
import java.util.List;

public class WebsocketController {
    WebsocketLinkApiImpl api;
    private MyPacketPreprocessor packetPreprocessor;
    List<Subscriber> packetSubsribers;
    List<String> linkNames = new ArrayList<>();

    //placeholder for now, eventually work on making a thread that looks for updates of the given param or link, if any updates are found then call the update from api
    public String getParamValue(String param) {
        return "test";

    }

    public void onParamUpdate(String linkName, byte[] param) {
        String packet = new String(param);
        api.onParameterUpdate(linkName, packet);
    }

    public void addLinkName(String linkName) {
        this.linkNames.add(linkName);
    }

    public void setPacketPreprocessor(MyPacketPreprocessor preprocessor) {
        packetPreprocessor = preprocessor;
    }

}
