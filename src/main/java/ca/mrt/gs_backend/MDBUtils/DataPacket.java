package ca.mrt.gs_backend.MDBUtils;

import ca.mrt.gs_backend.MDBUtils.dataPacketFormats.DataPacketInformation;
import lombok.Data;

import java.time.Instant;

/**
 * @author Tarek Namani
 * Defines what a DataPacket is like
 */
@Data
public class DataPacket {
    private int sequenceNumber;
    private String iSO_8601_generationTime;
    private Instant receptionTime;
    private Instant generationTime;
    private Instant earthReceptionTime;
    private DataPacketInformation dataPacketInformation = null;

    public DataPacket(int sequenceNumber, String iSO_8601_receptionTime, String iSO_8601_generationTime, String iSO_8601_earthReceptionTime) {
        this.sequenceNumber = sequenceNumber;
        this.iSO_8601_generationTime = iSO_8601_generationTime;
        this.receptionTime = Instant.parse(iSO_8601_receptionTime);
        this.generationTime = Instant.parse(iSO_8601_generationTime);
        this.earthReceptionTime = Instant.parse(iSO_8601_earthReceptionTime);
    }








}
