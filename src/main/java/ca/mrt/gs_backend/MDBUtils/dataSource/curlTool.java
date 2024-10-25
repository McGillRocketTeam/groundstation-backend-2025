package ca.mrt.gs_backend.MDBUtils.dataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Tarek Namani
 * Class used to send HTTP requests and fetch information from yamcs
 */
public class curlTool {

    /**
     * @param url : The URL containing the http request
     * @return : an HTTP response in JSON format
     */
    public String getHTTPResponse(String url)  {
        String returnVal = "";
        HttpURLConnection connection;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                returnVal = response.toString();
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnVal;
    }
}
