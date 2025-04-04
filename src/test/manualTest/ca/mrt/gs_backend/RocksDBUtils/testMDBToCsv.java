package manualTest.ca.mrt.gs_backend.RocksDBUtils;

import ca.mrt.gs_backend.MDBUtils.MdbToCsv;
import ca.mrt.gs_backend.MDBUtils.dataPacketFormats.LabjackT7Packet;
import ca.mrt.gs_backend.MDBUtils.dataPacketFormats.PacketFormat;
import ca.mrt.gs_backend.MDBUtils.dataSource.UdpSender;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * @author Tarek Namani and Davi Gava Bittencourt
 * Manual tests to acertain that the csv writer works
 */
public class testMDBToCsv {


    @Test
    public void testGetBytes() {
        MdbToCsv converter = new MdbToCsv();
        try {
            converter.writeToCsv("AAAAAAAAAAAAAAAAAAA.csv","http://localhost:8090/yamcs/api/archive/gs_backend/packets?&fields=id,generationTime,earthReceptionTime,receptionTime,sequenceNumber,link,size%200", PacketFormat.LABJACK);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test void testSendPackets() {
        UdpSender.sendPackets(10035,"src/test/manualTest/ca/mrt/gs_backend/RocksDBUtils/files/generatedPackets.raw");
        //UdpSender.sendPackets(27000,"src/test/manualTest/ca/mrt/gs_backend/RocksDBUtils/files/json_test.json");
    }

    @Test void testCreatePacket() {
        LabjackT7Packet packet = new LabjackT7Packet();

        String filePath = "generatedPackets.raw";
        int packetCount = 100; // Example number of packets

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(packet.getAsByteArray());
            System.out.println("File generated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
