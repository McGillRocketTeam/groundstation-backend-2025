package ca.mrt.gs_backend.MDBUtils.dataPacketFormats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Tarek Namani
 * This interface is used to define information held within a datapacket,
 * currently only defined for LabjackT7 packets
 */
public interface DataPacketInformation {
    String getInformationAsCSV();
    void getFromJSONArray(JsonArray jsonArray);
    String getAsFormattedString();


    /**
     * @param element : The JsonElement of a parameter of a datapacket
     * @return : a float representing the engValue of particular element
     * Since all formats have some floats to extract, this is common to both
     */
    default float getFloatFromJson(JsonElement element) {
        JsonElement map =(element.getAsJsonObject().get("engValue"));
        return ((JsonObject) map).get("floatValue").getAsFloat();
    }

    byte[] getAsByteArray();
}
