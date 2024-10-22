package ca.mrt.gs_backend.RocksDBUtils.dataPacketFormats;

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
}
