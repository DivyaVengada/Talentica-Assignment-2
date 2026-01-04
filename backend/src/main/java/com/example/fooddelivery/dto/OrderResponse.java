package com.example.fooddelivery.dto;

public class OrderResponse {
    private Long orderId;
    private String state;
    private Long restaurantId;
    private Long driverId;

    public OrderResponse(Long orderId, String state, Long restaurantId, Long driverId) {
        this.orderId = orderId;
        this.state = state;
        this.restaurantId = restaurantId;
        this.driverId = driverId;
    }

    public Long getOrderId() { return orderId; }
    public String getState() { return state; }
    public Long getRestaurantId() { return restaurantId; }
    public Long getDriverId() { return driverId; }
}
