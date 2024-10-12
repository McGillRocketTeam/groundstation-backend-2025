package ca.mrt.gs_backend.RocksDBUtils;

import org.rocksdb.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RocksDbToCsv {
    static {
        RocksDB.loadLibrary();
    }


    public static void main(String[] args) {
        try (final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction()) {
            String dbPath = "target/yamcs/yamcs-data/gs_backend.rdb/"; // Replace with your RocksDB path
            String csvPath = "export.csv";      // Output CSV file

            DBOptions  options = new DBOptions().setCreateIfMissing(false)
                    .setCreateMissingColumnFamilies(true);
            final List<ColumnFamilyDescriptor> cfDescriptors = Arrays.asList(
                    new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts),
                    new ColumnFamilyDescriptor("_metadata_".getBytes(), cfOpts),
                    new ColumnFamilyDescriptor("rt_data".getBytes(), cfOpts),
                    new ColumnFamilyDescriptor("parameter_archive".getBytes(), cfOpts)
            );

            final List<ColumnFamilyHandle> columnFamilyHandleList =
                    new ArrayList<>();

            try (RocksDB db = RocksDB.open(options, dbPath,cfDescriptors,columnFamilyHandleList);
                 BufferedWriter writer = new BufferedWriter(new FileWriter(csvPath))) {

                // Write the CSV header
                writer.write("key,value");
                writer.newLine();

                // Iterate over all key-value pairs in the RocksDB database
                RocksIterator iterator = db.newIterator();
                iterator.seekToFirst();
                System.out.println(db.get("apdoawpdoka".getBytes()));

                while (iterator.isValid()) {


                    iterator.next();
                }
            } catch (IOException e) {
                System.err.println("Error writing to CSV: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error accessing RocksDB: " + e.getMessage());
            }

        }
}
}
