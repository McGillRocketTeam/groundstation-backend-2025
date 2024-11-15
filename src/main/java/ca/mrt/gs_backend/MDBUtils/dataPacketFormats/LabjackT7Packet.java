package ca.mrt.gs_backend.MDBUtils.dataPacketFormats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Tarek Namani
 * This class defines the structure for a LabjackT7 datapacket, and contains some utility functions for parsing them
 */
@Data
public class LabjackT7Packet implements DataPacketInformation {

    private List<Float> AINPins = new ArrayList(14);
    private List<PinState> FIOPins = new ArrayList(8);
    private List<PinState> EIOPins = new ArrayList(8);
    private List<PinState> CIOPins = new ArrayList(4);
    private List<PinState> MIOPins = new ArrayList(3);

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

    @Override
    public byte[] getAsByteArray() {
        //format is AIN pins, then FIOB, EIO, CIO, then MIO

        List<PinState> lastFewPins = new ArrayList<>();
        lastFewPins.addAll(CIOPins);
        lastFewPins.addAll(MIOPins);

        byte[] returnedByte = ByteBuffer.allocate(7)
                .putFloat(AINPins.get(0))
                .putFloat(AINPins.get(1))
                .putFloat(AINPins.get(2))
                .putFloat(AINPins.get(3))
                .putFloat(AINPins.get(4))
                .putFloat(AINPins.get(5))
                .putFloat(AINPins.get(6))
                .putFloat(AINPins.get(7))
                .putFloat(AINPins.get(8))
                .putFloat(AINPins.get(9))
                .putFloat(AINPins.get(10))
                .putFloat(AINPins.get(11))
                .putFloat(AINPins.get(12))
                .putFloat(AINPins.get(13))
                .put((byte) getByteFromListOfPins(FIOPins,8))
                .put((byte) getByteFromListOfPins(EIOPins,8))
                .put((byte) getByteFromLastFewPins(lastFewPins))
                .array();

        return returnedByte;
    }


    private PinState getEnumFromJson (JsonElement element) {
        JsonObject map = (JsonObject) element.getAsJsonObject().get("engValue");
        String enumVal = (map.get("stringValue").toString().trim());

        if (enumVal.equals("\"low\"")) return PinState.low;
        else if (enumVal.equals("\"high\"")) return PinState.high;
        else if (enumVal.equals("\"unknown\"")) return PinState.unknown;
        else return PinState.undefined;

    }

    private int getByteFromListOfPins(List<PinState> pins, int size) {
        List<Integer> bits = pins.stream().map(this::getIntFromPinState).collect(Collectors.toList());
        int returnedByte = 0;

        for (int i = 0; i < size; i++) {
            returnedByte |= (bits.get(i) << (size - 1 - i)); // Shift each bit to its position
        }

        return  returnedByte;
    }
    private int getByteFromLastFewPins(List<PinState> pins) {
        List<Integer> bits = pins.stream().map(this::getIntFromPinState).collect(Collectors.toList());
        int returnedByte = 0;

        for (int i = 0; i < 7; i++) {
            returnedByte |= (bits.get(i) << (6 - i)); // Shift each bit to its position
        }

        returnedByte = returnedByte << 1;
        return  returnedByte;
    }


    private int getIntFromPinState(PinState state) {
        return switch (state) {
            case high -> 1;
            default -> 0;
        };
    }

    public LabjackT7Packet() {
        IntStream.range(0,14).forEach((i) -> AINPins.add(i,1.876876f));
        IntStream.range(0,8).forEach((i) -> FIOPins.add(i,PinState.high));
        IntStream.range(0,8).forEach((i) -> EIOPins.add(i,PinState.high));
        IntStream.range(0,3).forEach((i) -> CIOPins.add(i,PinState.high));
        IntStream.range(0,4).forEach((i) -> MIOPins.add(i,PinState.high));


    }




}
