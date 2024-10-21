package manualTest.ca.mrt.gs_backend.RocksDBUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSender {
    public static void main(String[] args) {
        // Configuration
        String host = "localhost"; // Change to your Yamcs server's IP address if needed
        int port = 10035; // Port configured in your UDP data link
        String filePath = "src/test/manualTest/ca/mrt/gs_backend/RocksDBUtils/files/packets - Copy.raw"; // Path to your raw file

        // Create a UDP socket
        // Create a UDP socket
        try (DatagramSocket socket = new DatagramSocket()) {
            // Read the raw file
            File file = new File(filePath);
            byte[] data = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(data); // Read the entire file into byte array
            }

            // Send data in smaller packets
            int offset = 0;
            int total_size = 0;
            while (offset < data.length) {
                int packetSize = Math.min(1400, data.length - offset);
                DatagramPacket packet = new DatagramPacket(data, offset, packetSize, InetAddress.getByName(host), port);
                socket.send(packet);
                System.out.println("Sent packet of size " + packetSize + " bytes to " + host + ":" + port);
                offset += packetSize; // Move to the next chunk
                total_size += packetSize;
            }
            System.out.println("Total packet size is " + total_size);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

