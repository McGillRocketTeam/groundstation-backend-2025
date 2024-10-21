package manualTest.ca.mrt.gs_backend.RocksDBUtils;

import ca.mrt.gs_backend.RocksDBUtils.RocksDbToCsv;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class testRocksDbToCsv {


    @Test
    public void testGetBytes() {
        RocksDbToCsv converter = new RocksDbToCsv();
        converter.setColumnHeaders(Arrays.asList("_metadata_","rt_data","parameter_archive"));
        try {
            converter.getParam();
        }catch (Exception e) {
            e.printStackTrace();
        }



    }


}
