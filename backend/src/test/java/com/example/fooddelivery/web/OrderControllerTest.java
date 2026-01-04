package com.example.fooddelivery.web;

import com.example.fooddelivery.domain.Driver;
import com.example.fooddelivery.domain.Order;
import com.example.fooddelivery.domain.OrderState;
import com.example.fooddelivery.domain.Restaurant;
import com.example.fooddelivery.dto.CreateOrderRequest;
import com.example.fooddelivery.dto.OrderResponse;
import com.example.fooddelivery.dto.TrackingResponse;
import com.example.fooddelivery.service.OrderService;
import com.example.fooddelivery.service.TrackingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    private OrderService orderService;
    private TrackingService trackingService;
    private OrderController controller;

    @BeforeEach
    void setup() {
        orderService = mock(OrderService.class);
        trackingService = mock(TrackingService.class);
        controller = new OrderController(orderService, trackingService);
    }

    @Test
    void create_shouldReturnOrderResponse() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setRestaurantId(1L);

        Restaurant r = new Restaurant("R1");
        r.setId(1L);

        Order o = new Order(r, 12.9, 77.6);
        o.setId(100L);
        o.setState(OrderState.CREATED);

        when(orderService.create(req)).thenReturn(o);

        OrderResponse resp = controller.create(req);

        assertEquals(100L, resp.getOrderId());
        assertEquals("CREATED", resp.getState());
        assertEquals(1L, resp.getRestaurantId());
        assertNull(resp.getDriverId());
    }

    @Test
    void assign_shouldReturnUpdatedOrderResponse() {
        Restaurant r = new Restaurant("R1");
        r.setId(1L);

        Driver d = new Driver("D1");
        d.setId(5L);

        Order o = new Order(r, 12.9, 77.6);
        o.setId(100L);
        o.setState(OrderState.DRIVER_ASSIGNED);
        o.setDriver(d);

        when(orderService.assignDriver(100L, 5L)).thenReturn(o);

        OrderResponse resp = controller.assign(100L, 5L);

        assertEquals(100L, resp.getOrderId());
        assertEquals("DRIVER_ASSIGNED", resp.getState());
        assertEquals(1L, resp.getRestaurantId());
        assertEquals(5L, resp.getDriverId());
    }

    @Test
    void pickup_shouldReturnOrderResponseWithDriverIfPresent() {
        Restaurant r = new Restaurant("R1");
        r.setId(1L);

        Driver d = new Driver("D1");
        d.setId(5L);

        Order o = new Order(r, 12.9, 77.6);
        o.setId(100L);
        o.setState(OrderState.PICKED_UP);
        o.setDriver(d);

        when(orderService.markPickedUp(100L)).thenReturn(o);

        OrderResponse resp = controller.pickup(100L);

        assertEquals(100L, resp.getOrderId());
        assertEquals("PICKED_UP", resp.getState());
        assertEquals(1L, resp.getRestaurantId());
        assertEquals(5L, resp.getDriverId());
    }

    @Test
    void tracking_shouldDelegateToTrackingService() {
        TrackingResponse tr = new TrackingResponse(100L, "PICKED_UP", 5L,
                12.9, 77.6, null, null, null, null, null, null);
        when(trackingService.getTracking(100L)).thenReturn(tr);

        TrackingResponse result = controller.tracking(100L);

        assertEquals(100L, result.getOrderId());
        verify(trackingService).getTracking(100L);
    }
}