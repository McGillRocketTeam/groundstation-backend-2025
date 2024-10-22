package ca.mrt.gs_backend.RocksDBUtils;
import ca.mrt.gs_backend.RocksDBUtils.dataPacketFormats.DataPacketInformation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Data
public class MdbToCsv {



    private List<String> columnHeaders = new ArrayList<>();


    public void writeToCsv(String csvPath) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvPath));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public List<DataPacket> getData(List<DataPacket> dataPackets) {
        dataPackets.stream().forEach(packet -> {
            int number = packet.getSequenceNumber();
            String generationTime = packet.getISO_8601_generationTime();
            System.out.println("Sequence Number: " + number);
            System.out.println("Generation Time: " + generationTime);
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append("http://localhost:8090/yamcs/api/archive/gs_backend/packets/%2FLabJackT7%2FLabJackPacket/")
                    .append(generationTime)
                    .append("/")
                    .append(number)
                    .append(":extract%200");
            String url = urlBuilder.toString();
            System.out.println(url);
            try {
                String response = getHTTPResponse(url);
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                System.out.println(jsonResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });



        return dataPackets;
    }

    private String getHTTPResponse(String url) throws IOException {
        String returnVal = "";
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            returnVal = response.toString();
            in.close();
        } else if (conn.getResponseCode() != 200) {
            System.out.println("GET request failed: " + conn.getResponseCode());
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            System.out.println("Error Response: " + errorResponse.toString());
        }
        else {
            System.out.println("GET request failed: " + conn.getResponseCode());
        }
        return returnVal;
    }

    public List<DataPacket> getPackets(){
        String response = "";
        try {
            response = getHTTPResponse("http://localhost:8090/yamcs/api/archive/gs_backend/packets?&fields=id,generationTime,earthReceptionTime,receptionTime,sequenceNumber,link,size%200");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        List<DataPacket> dataPackets = new ArrayList<>();
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

        JsonArray packets = jsonResponse.getAsJsonArray("packet");
            for (int i = 0; i < packets.size(); i++) {
                JsonObject packet = packets.get(i).getAsJsonObject();
                DataPacket dataPacket = new DataPacket(Integer.parseInt(packet.get("sequenceNumber").toString()),packet.get("receptionTime").getAsString(),packet.get("generationTime").getAsString(),packet.get("earthReceptionTime").getAsString());
                dataPackets.add(dataPacket);
            }
        return dataPackets;
    }
}
