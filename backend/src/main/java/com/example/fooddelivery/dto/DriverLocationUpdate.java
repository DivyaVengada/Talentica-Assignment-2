package com.example.fooddelivery.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class DriverLocationUpdate {
    private String eventId; // optional idempotency key

    @NotNull
    private Double lat;
    @NotNull
    private Double lng;
    private Integer heading;
    private Double speedMps;

    @NotNull
    private Instant timestamp;

    public String getEventId() { return eventId; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }
    public Integer getHeading() { return heading; }
    public Double getSpeedMps() { return speedMps; }
    public Instant getTimestamp() { return timestamp; }

    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setLat(Double lat) { this.lat = lat; }
    public void setLng(Double lng) { this.lng = lng; }
    public void setHeading(Integer heading) { this.heading = heading; }
    public void setSpeedMps(Double speedMps) { this.speedMps = speedMps; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
