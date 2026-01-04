package com.example.fooddelivery.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class TrackingResponse {
    private Long orderId;
    private String state;
    private Long driverId;
    private Double driverLat;
    private Double driverLng;
    private Instant locationTimestamp;
    private Integer etaMinSeconds;
    private Integer etaMaxSeconds;
    private Integer batchingStopsBeforeYou;
    private String batchingMessage;
    private Integer freshnessSeconds;

    public TrackingResponse(Long orderId, String state, Long driverId, Double driverLat, Double driverLng,
                            Instant locationTimestamp, Integer etaMinSeconds, Integer etaMaxSeconds,
                            Integer batchingStopsBeforeYou, String batchingMessage, Integer freshnessSeconds) {
        this.orderId = orderId;
        this.state = state;
        this.driverId = driverId;
        this.driverLat = driverLat;
        this.driverLng = driverLng;
        this.locationTimestamp = locationTimestamp;
        this.etaMinSeconds = etaMinSeconds;
        this.etaMaxSeconds = etaMaxSeconds;
        this.batchingStopsBeforeYou = batchingStopsBeforeYou;
        this.batchingMessage = batchingMessage;
        this.freshnessSeconds = freshnessSeconds;
    }

    public Long getOrderId() { return orderId; }
    public String getState() { return state; }
    public Long getDriverId() { return driverId; }
    public Double getDriverLat() { return driverLat; }
    public Double getDriverLng() { return driverLng; }
    public Instant getLocationTimestamp() { return locationTimestamp; }
    public Integer getEtaMinSeconds() { return etaMinSeconds; }
    public Integer getEtaMaxSeconds() { return etaMaxSeconds; }
    public Integer getBatchingStopsBeforeYou() { return batchingStopsBeforeYou; }
    public String getBatchingMessage() { return batchingMessage; }
    public Integer getFreshnessSeconds() { return freshnessSeconds; }
}
