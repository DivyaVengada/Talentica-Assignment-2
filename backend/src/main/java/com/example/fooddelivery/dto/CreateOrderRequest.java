package com.example.fooddelivery.dto;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {
    @NotNull
    private Long restaurantId;
    private double customerLat;
    private double customerLng;

    public Long getRestaurantId() { return restaurantId; }
    public double getCustomerLat() { return customerLat; }
    public double getCustomerLng() { return customerLng; }

    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public void setCustomerLat(double customerLat) { this.customerLat = customerLat; }
    public void setCustomerLng(double customerLng) { this.customerLng = customerLng; }
}
