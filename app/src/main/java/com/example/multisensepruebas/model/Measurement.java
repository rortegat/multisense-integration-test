package com.example.multisensepruebas.model;

import java.time.ZonedDateTime;
import java.util.List;

public class Measurement {
    private Long deviceId;
    private Location location;
    private List<MultiSenseBeacon> beacons;
    private String datetime;

    public Measurement() {
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<MultiSenseBeacon> getBeacons() {
        return beacons;
    }

    public void setBeacons(List<MultiSenseBeacon> beacons) {
        this.beacons = beacons;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
