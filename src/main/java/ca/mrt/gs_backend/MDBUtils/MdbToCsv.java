package ca.mrt.gs_backend.MDBUtils;
import ca.mrt.gs_backend.MDBUtils.dataPacketFormats.LabjackT7Packet;
import ca.mrt.gs_backend.MDBUtils.dataSource.curlTool;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class MdbToCsv {

    private curlTool tool = new curlTool();

   public void writeToCsv(String csvPath) {
       List<DataPacket> packets = getPackets();
       getData(packets);
        StringBuilder builder = new StringBuilder();
       try {
           BufferedWriter writer = new BufferedWriter(new FileWriter(csvPath),256000);
        packets.forEach(packet -> {
            builder.append(packet.getSequenceNumber()).append(",");
            builder.append(packet.getGenerationTime()).append(",");
            builder.append(packet.getReceptionTime()).append(",");
            builder.append(packet.getDataPacketInformation().getInformationAsCSV()).append("\n");
            String toWrite = builder.toString();
            builder.delete(0, builder.length());
            System.out.println("wrote");
            try {
                writer.write(toWrite);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });




        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getData(List<DataPacket> dataPackets) {
        dataPackets.forEach(packet -> {
            packet.setDataPacketInformation(new LabjackT7Packet());
            int number = packet.getSequenceNumber();
            String generationTime = packet.getISO_8601_generationTime();

            String url = "http://localhost:8090/yamcs/api/archive/gs_backend/packets/%2FLabJackT7%2FLabJackPacket/" +
                    generationTime +
                    "/" +
                    number +
                    ":extract";
            try {
                String response = tool.getHTTPResponse(url);
                JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                JsonArray params = jsonResponse.getAsJsonArray("parameterValues");
                packet.getDataPacketInformation().getFromJSONArray(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public List<DataPacket> getPackets(){
        String response = "";
        try {
            response = tool.getHTTPResponse("http://localhost:8090/yamcs/api/archive/gs_backend/packets?&fields=id,generationTime,earthReceptionTime,receptionTime,sequenceNumber,link,size%200");
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<DataPacket> dataPackets = new ArrayList<>();
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

        JsonArray packets = jsonResponse.getAsJsonArray("packet");
            for (int i = 0; i < packets.size(); i++) {
                JsonObject packet = packets.get(i).getAsJsonObject();
                DataPacket dataPacket = new DataPacket(Integer.parseInt(packet.get("sequenceNumber").toString()),packet.get("receptionTime").getAsString(),packet.get("generationTime").getAsString(),packet.get("earthReceptionTime").getAsString());
                dataPackets.add(dataPacket);
            }
        return dataPackets;
    }
}
