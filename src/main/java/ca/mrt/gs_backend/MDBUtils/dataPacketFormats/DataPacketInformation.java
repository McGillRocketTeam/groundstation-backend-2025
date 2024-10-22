package ca.mrt.gs_backend.MDBUtils.dataPacketFormats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface DataPacketInformation {
    public String getInformationAsCSV();
    public void getFromJSONArray(JsonArray jsonArray);


    default float getFloatFromJson(JsonElement element) {
        JsonElement map =(element.getAsJsonObject().get("engValue"));
        return ((JsonObject) map).get("floatValue").getAsFloat();
    }
}
