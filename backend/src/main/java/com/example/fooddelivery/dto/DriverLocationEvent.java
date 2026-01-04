package com.example.fooddelivery.dto;

import java.time.Instant;

public class DriverLocationEvent {
    private Long driverId;
    private String eventId;
    private double lat;
    private double lng;
    private Integer heading;
    private Double speedMps;
    private Instant timestamp;

    public DriverLocationEvent() {}

    public DriverLocationEvent(Long driverId, String eventId, double lat, double lng, Integer heading, Double speedMps, Instant timestamp) {
        this.driverId = driverId;
        this.eventId = eventId;
        this.lat = lat;
        this.lng = lng;
        this.heading = heading;
        this.speedMps = speedMps;
        this.timestamp = timestamp;
    }

    public Long getDriverId() { return driverId; }
    public String getEventId() { return eventId; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public Integer getHeading() { return heading; }
    public Double getSpeedMps() { return speedMps; }
    public Instant getTimestamp() { return timestamp; }

    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setHeading(Integer heading) { this.heading = heading; }
    public void setSpeedMps(Double speedMps) { this.speedMps = speedMps; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
