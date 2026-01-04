package com.example.fooddelivery.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {
    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState state = OrderState.CREATED;

    @Column(name = "customer_lat", nullable = false)
    private double customerLat;

    @Column(name = "customer_lng", nullable = false)
    private double customerLng;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Order() {}

    public Order(Restaurant restaurant, double customerLat, double customerLng) {
        this.restaurant = restaurant;
        this.customerLat = customerLat;
        this.customerLng = customerLng;
    }

    public Long getId() { return id; }
    public Restaurant getRestaurant() { return restaurant; }
    public OrderState getState() { return state; }
    public double getCustomerLat() { return customerLat; }
    public double getCustomerLng() { return customerLng; }
    public Driver getDriver() { return driver; }
    public Instant getPickedUpAt() { return pickedUpAt; }

    public void setDriver(Driver driver) { this.driver = driver; }
    public void setState(OrderState state) { this.state = state; }
    public void setPickedUpAt(Instant pickedUpAt) { this.pickedUpAt = pickedUpAt; }
}
