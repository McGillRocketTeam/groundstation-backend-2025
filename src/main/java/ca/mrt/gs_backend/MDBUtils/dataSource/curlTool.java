package ca.mrt.gs_backend.MDBUtils.dataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class curlTool {

    public String getHTTPResponse(String url) throws IOException {
        String returnVal = "";
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() == 200) {
            System.out.println(conn.getURL());
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            returnVal = response.toString();

            in.close();
        } else if (conn.getResponseCode() != 200) {
            System.out.println("GET request failed: " + conn.getResponseCode());

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            System.out.println("Error Response: " + errorResponse.toString());
        }
        else {
            System.out.println("GET request failed: " + conn.getResponseCode());
        }
        return returnVal;
    }
}
