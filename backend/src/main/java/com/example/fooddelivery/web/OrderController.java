package com.example.fooddelivery.web;

import com.example.fooddelivery.domain.Order;
import com.example.fooddelivery.dto.CreateOrderRequest;
import com.example.fooddelivery.dto.OrderResponse;
import com.example.fooddelivery.dto.TrackingResponse;
import com.example.fooddelivery.service.OrderService;
import com.example.fooddelivery.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final TrackingService trackingService;

    public OrderController(OrderService orderService, TrackingService trackingService) {
        this.orderService = orderService;
        this.trackingService = trackingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        Order o = orderService.create(request);
        return new OrderResponse(o.getId(), o.getState().name(), o.getRestaurant().getId(), null);
    }

    @PostMapping("/{orderId}/assign/{driverId}")
    public OrderResponse assign(@PathVariable Long orderId, @PathVariable Long driverId) {
        Order o = orderService.assignDriver(orderId, driverId);
        return new OrderResponse(o.getId(), o.getState().name(), o.getRestaurant().getId(), o.getDriver().getId());
    }

    @PostMapping("/{orderId}/pickup")
    public OrderResponse pickup(@PathVariable Long orderId) {
        Order o = orderService.markPickedUp(orderId);
        Long driverId = (o.getDriver() == null) ? null : o.getDriver().getId();
        return new OrderResponse(o.getId(), o.getState().name(), o.getRestaurant().getId(), driverId);
    }

    @GetMapping("/{orderId}/tracking")
    public TrackingResponse tracking(@PathVariable Long orderId) {
        return trackingService.getTracking(orderId);
    }
}
