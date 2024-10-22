package ca.mrt.gs_backend.RocksDBUtils.dataPacketFormats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class LabjackT7Packet implements DataPacketInformation {

    private List<Float> AINPins = new ArrayList<Float>(14);
    private List<PinState> FIOPins = new ArrayList<PinState>(8);
    private List<PinState> EIOPins = new ArrayList<PinState>(8);
    private List<PinState> CIOPins = new ArrayList<PinState>(4);
    private List<PinState> MIOPins = new ArrayList<PinState>(3);

    public String getInformationAsCSV() {


        return "";
    }


    public void getFromJSONArray(JsonArray jsonArray) {
        AINPins.add(0, getFloatFromJson(jsonArray.get(0)));
        AINPins.add(1, getFloatFromJson(jsonArray.get(1)));
        AINPins.add(2, getFloatFromJson(jsonArray.get(2)));
        AINPins.add(3, getFloatFromJson(jsonArray.get(3)));
        AINPins.add(4, getFloatFromJson(jsonArray.get(4)));
        AINPins.add(5, getFloatFromJson(jsonArray.get(5)));
        AINPins.add(6, getFloatFromJson(jsonArray.get(6)));
        AINPins.add(7, getFloatFromJson(jsonArray.get(7)));
        AINPins.add(8, getFloatFromJson(jsonArray.get(8)));
        AINPins.add(9, getFloatFromJson(jsonArray.get(9)));
        AINPins.add(10, getFloatFromJson(jsonArray.get(10)));
        AINPins.add(11, getFloatFromJson(jsonArray.get(11)));
        AINPins.add(12, getFloatFromJson(jsonArray.get(11)));
        AINPins.add(13, getFloatFromJson(jsonArray.get(13)));

        FIOPins.add(0, getEnumFromJson(jsonArray.get(14)));
        FIOPins.add(1, getEnumFromJson(jsonArray.get(15)));
        FIOPins.add(2, getEnumFromJson(jsonArray.get(16)));
        FIOPins.add(3, getEnumFromJson(jsonArray.get(17)));
        FIOPins.add(4, getEnumFromJson(jsonArray.get(18)));
        FIOPins.add(5, getEnumFromJson(jsonArray.get(19)));
        FIOPins.add(6, getEnumFromJson(jsonArray.get(20)));
        FIOPins.add(7, getEnumFromJson(jsonArray.get(21)));

        EIOPins.add(0, getEnumFromJson(jsonArray.get(22)));
        EIOPins.add(1, getEnumFromJson(jsonArray.get(23)));
        EIOPins.add(2, getEnumFromJson(jsonArray.get(24)));
        EIOPins.add(3, getEnumFromJson(jsonArray.get(25)));
        EIOPins.add(4, getEnumFromJson(jsonArray.get(26)));
        EIOPins.add(5, getEnumFromJson(jsonArray.get(27)));
        EIOPins.add(6, getEnumFromJson(jsonArray.get(28)));
        EIOPins.add(7, getEnumFromJson(jsonArray.get(29)));

        CIOPins.add(0, getEnumFromJson(jsonArray.get(30)));
        CIOPins.add(1, getEnumFromJson(jsonArray.get(31)));
        CIOPins.add(2, getEnumFromJson(jsonArray.get(32)));
        CIOPins.add(3, getEnumFromJson(jsonArray.get(33)));

        MIOPins.add(0, getEnumFromJson(jsonArray.get(34)));
        MIOPins.add(1, getEnumFromJson(jsonArray.get(35)));
        MIOPins.add(2, getEnumFromJson(jsonArray.get(36)));

    }


    private PinState getEnumFromJson (JsonElement element) {
        JsonObject map = (JsonObject) element.getAsJsonObject().get("engValue");
        String enumVal = (map.get("stringValue").toString().trim());
        if (enumVal.equals("\"low\"")) return PinState.low;
        else if (enumVal.equals("\"high\"")) return PinState.high;
        else if (enumVal.equals("\"unknown\"")) return PinState.unknown;
        else return PinState.undefined;
    }


}
