package ca.mrt.gs_backend.MDBUtils;
import ca.mrt.gs_backend.MDBUtils.dataPacketFormats.LabjackT7Packet;
import ca.mrt.gs_backend.MDBUtils.dataPacketFormats.PacketFormat;
import ca.mrt.gs_backend.MDBUtils.dataSource.curlTool;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tarek Namani
 * Main class that handles conversion of datapackets to csv
 */
@Data
public class MdbToCsv {

    private curlTool tool = new curlTool();


    /**
     * @param csvPath : destination path of CSV
     * @param url : URL of packets to extract
     * Main function of this class, handles getting data from the
     */
   public void writeToCsv(String csvPath, String url, PacketFormat format) {
       List<DataPacket> packets = getPackets(url); //gets the packets, format specific information is still null
       switch (format) { //extracts format specific information
           case LABJACK -> getLabjackData(packets);
           case FC1 -> System.out.println("No FC1 implementation yet");
           case FC2 -> System.out.println("No FC2 implementation yet");
       }

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

    /**
     * @param dataPackets list of Data Packets to update
     * updates dataPackets with the correct Labjack packet information
     */
    public void getLabjackData(List<DataPacket> dataPackets) {
        dataPackets.forEach(packet -> {
            packet.setDataPacketInformation(new LabjackT7Packet());
            int number = packet.getSequenceNumber();
            String generationTime = packet.getISO_8601_generationTime();

            String url = "http://localhost:8090/yamcs/api/archive/gs_backend/packets/%2FLabJackT7%2FLabJackPacket/" +
                    generationTime +
                    "/" +
                    number +
                    ":extract";

            getParameters(packet, url); //gets parameters for each packet
        });

    }


    /**
     * @param url : URL of packet list
     * @return A list of Data Packets
     * This class gets a list of Data packets from the specified URL, with the common values shared between packet formats
     */
    public List<DataPacket> getPackets(String url){


        List<DataPacket> dataPackets = new ArrayList<>();
        String continuationToken = "";

        JsonObject response = getJsonResponse(url); //gets http response
        JsonArray packets = response.getAsJsonArray("packet");

        if (response.getAsJsonObject().get("continuationToken") != null) continuationToken = response.getAsJsonObject().get("continuationToken").getAsString(); //continuation token is used to make successive calls to yamcs
        dataPackets.addAll(extractDataPackets(packets));

        while (!continuationToken.isEmpty()) { //if more than 100 packets are on the yamcs server, multiple requests are needed
            String newUrl = url + "&next=" + continuationToken; //generate a successive http request based on the response
            response = getJsonResponse(newUrl);
            packets = response.getAsJsonArray("packet");
            if (response.getAsJsonObject().get("continuationToken") != null) continuationToken = response.getAsJsonObject().get("continuationToken").getAsString();
            List<DataPacket> newPackets = extractDataPackets(packets);
            if (assertNotDuplicate(dataPackets,newPackets)) break;//continuation tokens will always be generated, end once we duplicate any packet
            dataPackets.addAll(newPackets);
        }

        return dataPackets;
    }

    /**
     * @param packets json array of packets
     * @return a list of Data Packets
     * Creates a list of Data Packets and sets the sequenceNumber, reception, generation and earthReception times
     */
    private List<DataPacket>  extractDataPackets(JsonArray packets) {
        List<DataPacket> packetsToAdd = new ArrayList<>();
        for (int i = 0; i < packets.size(); i++) {
            JsonObject packet = packets.get(i).getAsJsonObject();
            DataPacket dataPacket = new DataPacket(Integer.parseInt(packet.get("sequenceNumber").toString()), packet.get("receptionTime").getAsString(), packet.get("generationTime").getAsString(), packet.get("earthReceptionTime").getAsString());
            packetsToAdd.add(dataPacket);
        }
        return packetsToAdd;
    }

    /**
     * @param packets existing list of packets
     * @param newPackets packets to add
     * @return boolean : whether newPackets is a subset of packets or not
     */
    private boolean assertNotDuplicate(List <DataPacket> packets, List <DataPacket> newPackets) {
        Set<Integer> packetSet = packets.stream().map(DataPacket::getSequenceNumber).collect(Collectors.toSet());
        int sizeBefore = packetSet.size();
        newPackets.stream().forEach(packet -> {packetSet.add(packet.getSequenceNumber());});
       return sizeBefore == packetSet.size();
    }

    /**
     * @param url : HTTP request
     * @return JSON response of request
     */
    private JsonObject getJsonResponse (String url) {
        String response = "";
        try {
            response = tool.getHTTPResponse(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        return jsonResponse;
    }

    /**
     * @param packet : DataPacket to update with format specific information
     * @param url : URL of information
     * Extracts the parameters and assigns them to the correct values in the format-specific class
     */
    private void getParameters(DataPacket packet, String url) {
        try {
            String response = tool.getHTTPResponse(url);
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            JsonArray params = jsonResponse.getAsJsonArray("parameterValues"); //fetches parameters
            packet.getDataPacketInformation().getFromJSONArray(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
