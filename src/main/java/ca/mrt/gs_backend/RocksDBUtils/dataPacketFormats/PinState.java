package ca.mrt.gs_backend.RocksDBUtils.dataPacketFormats;


public enum PinState {
    high,
    low,
    unknown;

public String toString() {
    switch (this) {
        case high: return "high";
        case low: return "low";
        case unknown: return "unknown";
    }
    return "";
}

}
