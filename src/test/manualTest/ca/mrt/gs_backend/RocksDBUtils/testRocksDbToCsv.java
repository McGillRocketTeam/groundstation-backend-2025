package manualTest.ca.mrt.gs_backend.RocksDBUtils;

import ca.mrt.gs_backend.RocksDBUtils.DataPacket;
import ca.mrt.gs_backend.RocksDBUtils.MdbToCsv;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class testRocksDbToCsv {


    @Test
    public void testGetBytes() {
        MdbToCsv converter = new MdbToCsv();
        converter.setColumnHeaders(Arrays.asList("_metadata_","rt_data","parameter_archive"));
        try {
            List<DataPacket> packets = converter.getPackets();
            converter.getData(packets);
            System.out.println(packets);
        }catch (Exception e) {
            e.printStackTrace();
        }





    }


}
