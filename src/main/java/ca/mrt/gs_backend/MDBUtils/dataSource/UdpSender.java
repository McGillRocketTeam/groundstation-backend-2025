package ca.mrt.gs_backend.MDBUtils.dataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSender {

    public static void sendPackets(int port, String filePath) {
        int packetSize = 59; //size of a packet 59 for Labjack
        String host = "localhost";

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
                int size = Math.min(packetSize, data.length - offset);
                DatagramPacket packet = new DatagramPacket(data, offset, size, InetAddress.getByName(host), port);
                socket.send(packet);
                offset += size;
                total_size += size;
            }
            System.out.println("Total packet size is " + total_size);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


}

