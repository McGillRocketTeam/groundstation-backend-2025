package ca.mrt.gs_backend.MDBUtils.dataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tarek Namani and Davi Gava Bittencourt
 * A utility class for manually sending data packets over udp to yamcs
 */
public class UdpSender {

    /**
     * @param port : Port of the datalink to send the packet to
     * @param filePath : Path of the file to send (either RAW or JSON)
     */
    public static void sendPackets(int port, String filePath) {
        int packetSize = 59; //size of a packet 59 for Labjack
        String host = "localhost";

        try (DatagramSocket socket = new DatagramSocket()) {
            File file = new File(filePath);
            byte[] data;

            if (isJsonFile(file)) {
                data = parseJsonFile(file);
            } else if (isRawFile(file)) {
                data = parseRawFile(file);
            } else {
                throw new IllegalArgumentException("File is neither a raw nor JSON file");
            }

            int offset = 0;
            int total_size = 0;
            while (offset < data.length) { //source is raw data, iterate through

                int size = Math.min(packetSize, data.length - offset);
                DatagramPacket packet = new DatagramPacket(data, offset, size, InetAddress.getByName(host), port);
                socket.send(packet); //send each packet individually
                offset += size;
                total_size += size;
            }
            System.out.println("Total packet size is " + total_size);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * @param file : File to check
     * @return true if the file is JSON, false otherwise
     */
    private static boolean isJsonFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".json");
    }

    /**
     * @param file : File to check
     * @return true if the file is RAW, false otherwise
     */
    private static boolean isRawFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".raw");
    }

    /**
     * @param file : JSON file
     * @return byte array extracted from the JSON "data" field
     * @throws IOException if the file cannot be read or parsed
     */
    private static byte[] parseJsonFile(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(file);

        if (rootNode.has("data") && rootNode.get("data").isArray()) {
            byte[] data = new byte[rootNode.get("data").size()];
            for (int i = 0; i < rootNode.get("data").size(); i++) {
                data[i] = (byte) rootNode.get("data").get(i).asInt();
            }
            return data;
        } else {
            throw new IOException("JSON file does not contain a 'data' array.");
        }
    }

    /**
     * @param file : Path to the raw binary file
     * @return byte array extracted from the RAW file
     * @throws IOException if the file cannot be read
     */
    private static byte[] parseRawFile(File file) throws IOException {
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(data);
        }
        return data;
    }

}

