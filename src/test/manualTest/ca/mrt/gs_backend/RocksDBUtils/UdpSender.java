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
        int shmolSize = 59;
        String host = "localhost";
        int port = 10035;
        String filePath = "src/test/manualTest/ca/mrt/gs_backend/RocksDBUtils/files/packets - Copy.raw";


        try (DatagramSocket socket = new DatagramSocket()) {
            // Read the raw file
            File file = new File(filePath);
            byte[] data = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(data);
            }


            int offset = 0;
            int total_size = 0;
            while (offset < data.length) {
                int packetSize = Math.min(shmolSize, data.length - offset);
                DatagramPacket packet = new DatagramPacket(data, offset, packetSize, InetAddress.getByName(host), port);
                socket.send(packet);
                System.out.println("Sent packet of size " + packetSize + " bytes to " + host + ":" + port);
                offset += packetSize;
                total_size += packetSize;
            }
            System.out.println("Total packet size is " + total_size);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

