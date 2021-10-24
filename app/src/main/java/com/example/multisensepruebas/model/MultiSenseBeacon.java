package com.example.multisensepruebas.model;

import java.util.Map;

public class MultiSenseBeacon {
    
    private Long beaconId;
    private Map<String, Object> telemetry;

    public Long getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(Long beaconId) {
        this.beaconId = beaconId;
    }

    public Map<String, Object> getTelemetry() {
        return telemetry;
    }

    public void setTelemetry(Map<String, Object> telemetry) {
        this.telemetry = telemetry;
    }

    @Override
    public String toString() {
        return "MultiSenseBeacon{" +
                "beaconId=" + beaconId +
                ", telemetry=" + telemetry +
                '}';
    }
}
