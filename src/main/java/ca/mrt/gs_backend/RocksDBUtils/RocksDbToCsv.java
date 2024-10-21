package ca.mrt.gs_backend.RocksDBUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import org.rocksdb.*;
import org.yamcs.YamcsServer;
import org.yamcs.api.Observer;
import org.yamcs.http.Context;
import org.yamcs.http.api.InstancesApi;
import org.yamcs.http.api.MdbApi;
import org.yamcs.http.api.StreamArchiveApi;
import org.yamcs.mdb.Mdb;
import org.yamcs.mdb.MdbFactory;
import org.yamcs.protobuf.Archive;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
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

                int numPairs = 0;
                while (iterator.isValid()) {
                    System.out.print("Key :");
                    System.out.println(getString(iterator.key()));
                    System.out.print("Value :");
                    System.out.println(getString(iterator.value()));
                    iterator.next();
                    numPairs ++;
                }
                System.out.println("Number of pairs : " + numPairs);
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

    public void getParam() throws IOException {
        String url = "http://localhost:8090/yamcs/api/mdb/gs_backend/parameters?system=/LabJackT7&details=true&pos=0&limit=100";
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse JSON
            Gson gson = new Gson();
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

            // Example: Accessing parameters
            JsonArray parameters = jsonResponse.getAsJsonArray("parameters");
            for (int i = 0; i < parameters.size(); i++) {
                JsonObject parameter = parameters.get(i).getAsJsonObject();
                String name = parameter.get("name").getAsString();
                String path = parameter.get("qualifiedName").getAsString();
                String type = parameter.get("type").getAsString();
                String dataSource = parameter.get("dataSource").getAsString();
                System.out.println(name + "   " + path + "   " +  type + "   " + dataSource);
            }
        } else {
            System.out.println("GET request failed: " + conn.getResponseCode());
        }
    }
}
