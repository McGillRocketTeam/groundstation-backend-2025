package manualTest.ca.mrt.gs_backend.RocksDBUtils;

import ca.mrt.gs_backend.MDBUtils.MdbToCsv;
import ca.mrt.gs_backend.MDBUtils.dataPacketFormats.PacketFormat;
import ca.mrt.gs_backend.MDBUtils.dataSource.UdpSender;
import org.junit.jupiter.api.Test;

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
        UdpSender.sendPackets(10035,"src/test/manualTest/ca/mrt/gs_backend/RocksDBUtils/files/packets - Copy.raw");
        UdpSender.sendPackets(27000,"src/test/manualTest/ca/mrt/gs_backend/RocksDBUtils/files/json_test.json");
    }

}
