package manualTest.ca.mrt.gs_backend.RocksDBUtils;

import ca.mrt.gs_backend.MDBUtils.DataPacket;
import ca.mrt.gs_backend.MDBUtils.MdbToCsv;
import ca.mrt.gs_backend.MDBUtils.dataSource.UdpSender;
import org.junit.jupiter.api.Test;
import java.util.List;

public class testMDBToCsv {


    @Test
    public void testGetBytes() {
        MdbToCsv converter = new MdbToCsv();
        try {
            List<DataPacket> packets = converter.getPackets();
            converter.getData(packets);
            System.out.println(packets.size());
        }catch (Exception e) {
            e.printStackTrace();
        }

        converter.writeToCsv("AAAAAAAAAAAAAAAAAAA.csv");


    }

    @Test void testSendPackets() {
        UdpSender.sendPackets(10035,"src/test/manualTest/ca/mrt/gs_backend/RocksDBUtils/files/packets - Copy.raw");
    }


}
