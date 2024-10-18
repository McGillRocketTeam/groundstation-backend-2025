package ca.mrt.gs_backend.RocksDBUtils;

import lombok.Data;
import org.rocksdb.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
                    System.out.println(getString(iterator.key()));
                    System.out.print("Value :");
                    System.out.println(getString(iterator.value()));
                    iterator.next();
                }
            } catch (RocksDBException e) {
                throw new RuntimeException(e);
            }

        }

        return new Byte[0];
    }

    private static String getString(byte[] array) {
        String inAscii = Arrays.toString(array).replace("[", "").replace("]", "").replace(",", "");
        String result = Arrays.stream(inAscii.split(" "))
                .map(c -> String.valueOf((char) Integer.parseInt(c)))
                .collect(Collectors.joining());
        return result;
    }

    public void updateWith(String data, String dbPath) {
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

                db.put(data.getBytes(),data.getBytes());
            } catch (RocksDBException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
