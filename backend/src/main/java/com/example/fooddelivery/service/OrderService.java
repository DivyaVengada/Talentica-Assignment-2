package com.example.fooddelivery.service;

import com.example.fooddelivery.domain.*;
import com.example.fooddelivery.dto.CreateOrderRequest;
import com.example.fooddelivery.repository.DriverRepository;
import com.example.fooddelivery.repository.OrderRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final DriverRepository driverRepository;

    public OrderService(OrderRepository orderRepository,
                        RestaurantRepository restaurantRepository,
                        DriverRepository driverRepository) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.driverRepository = driverRepository;
    }

    public Order create(CreateOrderRequest request) {
        Restaurant r = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        Order o = new Order(r, request.getCustomerLat(), request.getCustomerLng());
        o.setState(OrderState.CREATED);
        return orderRepository.save(o);
    }

    public Order assignDriver(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
        order.setDriver(driver);
        order.setState(OrderState.DRIVER_ASSIGNED);
        return orderRepository.save(order);
    }

    public Order markPickedUp(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setPickedUpAt(Instant.now());
        order.setState(OrderState.PICKED_UP);
        return orderRepository.save(order);
    }
}
