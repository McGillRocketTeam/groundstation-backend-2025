package ca.mrt.gs_backend.MDBUtils.dataPacketFormats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class LabjackT7Packet implements DataPacketInformation {

    private List<Float> AINPins = new ArrayList<Float>(14);
    private List<PinState> FIOPins = new ArrayList<PinState>(8);
    private List<PinState> EIOPins = new ArrayList<PinState>(8);
    private List<PinState> CIOPins = new ArrayList<PinState>(4);
    private List<PinState> MIOPins = new ArrayList<PinState>(3);

    public String getInformationAsCSV() {

        StringBuilder builder = new StringBuilder();
        builder.append(AINPins.stream().map(f -> f.toString()).collect(Collectors.joining(",")));
        builder.append(",");
        builder.append(FIOPins.stream().map(f -> f.toString()).collect(Collectors.joining(",")));
        builder.append(",");
        builder.append(EIOPins.stream().map(f -> f.toString()).collect(Collectors.joining(",")));
        builder.append(",");
        builder.append(CIOPins.stream().map(f -> f.toString()).collect(Collectors.joining(",")));
        builder.append(",");
        builder.append(MIOPins.stream().map(f -> f.toString()).collect(Collectors.joining(",")));
        return builder.toString();

    }


    public void getFromJSONArray(JsonArray jsonArray) {

        IntStream.range(0,14).forEach(i ->AINPins.add(i,getFloatFromJson(jsonArray.get(i))));
        IntStream.range(0,8).forEach(i ->FIOPins.add(i,getEnumFromJson(jsonArray.get(i+14))));
        IntStream.range(0,8).forEach(i ->EIOPins.add(i,getEnumFromJson(jsonArray.get(i+22))));
        IntStream.range(0,4).forEach(i ->CIOPins.add(i,getEnumFromJson(jsonArray.get(i+30))));
        IntStream.range(0,3).forEach(i ->MIOPins.add(i,getEnumFromJson(jsonArray.get(i+34))));

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
