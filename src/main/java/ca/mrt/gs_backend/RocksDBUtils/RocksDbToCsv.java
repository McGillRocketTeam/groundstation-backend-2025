package ca.mrt.gs_backend.RocksDBUtils;

import lombok.Data;
import lombok.Getter;
import org.openjdk.nashorn.internal.objects.annotations.Setter;
import org.rocksdb.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Data
public class RocksDbToCsv {
    static {
        RocksDB.loadLibrary();
    }


    private List<String> columnHeaders = new ArrayList<>();


    public void writeToCsv(String dbPath, String csvPath) {
        Byte[] byteArray = getDbContent(dbPath);
        List<Integer> bitArray= getBitArrayFromDB(byteArray);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvPath));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public List<Integer> getBitArrayFromDB(Byte[] dbContent) {
        List<Integer> returnList = new ArrayList<>();



        return returnList;
    }

    public Byte[] getDbContent(String dbPath) {
        try (final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction()) {

            //create options for accesing DB
            DBOptions  options = new DBOptions().setCreateIfMissing(false)
                    .setCreateMissingColumnFamilies(true);
            List<ColumnFamilyDescriptor> cfDescriptors = new ArrayList<>();

            //set up Column family headers
            cfDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts));
            columnHeaders.stream().forEach(family -> {
                ColumnFamilyDescriptor descriptor =new ColumnFamilyDescriptor(family.getBytes(), cfOpts);
                cfDescriptors.add(descriptor);
            });

            final List<ColumnFamilyHandle> columnFamilyHandleList = new ArrayList<>();

            //iterate over DB
            try (RocksDB db = RocksDB.open(options, dbPath,cfDescriptors,columnFamilyHandleList)) {

                RocksIterator iterator = db.newIterator();

                iterator.seekToFirst();

                while (iterator.isValid()) {
                    System.out.print("Key :");
                    System.out.println(Arrays.toString(iterator.key()));
                    System.out.print("Value :");
                    System.out.println(Arrays.toString(iterator.value()));
                    iterator.next();
                }
            } catch (RocksDBException e) {
                throw new RuntimeException(e);
            }

        }

        return new Byte[0];
    }
}
