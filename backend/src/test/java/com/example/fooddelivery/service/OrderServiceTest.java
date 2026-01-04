package com.example.fooddelivery.service;

import com.example.fooddelivery.domain.*;
import com.example.fooddelivery.dto.CreateOrderRequest;
import com.example.fooddelivery.repository.DriverRepository;
import com.example.fooddelivery.repository.OrderRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private RestaurantRepository restaurantRepository;
    private DriverRepository driverRepository;

    private OrderService orderService;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        restaurantRepository = mock(RestaurantRepository.class);
        driverRepository = mock(DriverRepository.class);

        orderService = new OrderService(orderRepository, restaurantRepository, driverRepository);
    }

    @Test
    void create_shouldCreateOrderForExistingRestaurant() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setRestaurantId(1L);
        req.setCustomerLat(12.9);
        req.setCustomerLng(77.6);

        Restaurant r = new Restaurant("R1");
        r.setId(1L);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(r));

        Order saved = new Order(r, 12.9, 77.6);
        saved.setId(100L);
        saved.setState(OrderState.CREATED);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        Order result = orderService.create(req);

        assertEquals(100L, result.getId());
        assertEquals(OrderState.CREATED, result.getState());
        assertEquals(r, result.getRestaurant());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void create_shouldFailIfRestaurantNotFound() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setRestaurantId(1L);
        req.setCustomerLat(12.9);
        req.setCustomerLng(77.6);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.create(req));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void assignDriver_shouldAssignDriverAndUpdateState() {
        Order order = new Order();
        order.setId(10L);
        order.setState(OrderState.CREATED);

        Driver driver = new Driver("D1");
        driver.setId(5L);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(driverRepository.findById(5L)).thenReturn(Optional.of(driver));

        Order saved = new Order();
        saved.setId(10L);
        saved.setState(OrderState.DRIVER_ASSIGNED);
        saved.setDriver(driver);
        when(orderRepository.save(order)).thenReturn(saved);

        Order result = orderService.assignDriver(10L, 5L);

        assertEquals(OrderState.DRIVER_ASSIGNED, result.getState());
        assertNotNull(result.getDriver());
        assertEquals(5L, result.getDriver().getId());
    }

    @Test
    void assignDriver_shouldFailIfOrderNotFound() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.assignDriver(10L, 5L));
    }

    @Test
    void assignDriver_shouldFailIfDriverNotFound() {
        Order order = new Order();
        order.setId(10L);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(driverRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.assignDriver(10L, 5L));
    }

    @Test
    void markPickedUp_shouldSetPickedUpAtAndState() {
        Order order = new Order();
        order.setId(10L);
        order.setState(OrderState.DRIVER_ASSIGNED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.markPickedUp(10L);

        assertEquals(OrderState.PICKED_UP, result.getState());
        assertNotNull(result.getPickedUpAt());
    }

    @Test
    void markPickedUp_shouldFailIfOrderNotFound() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.markPickedUp(10L));
    }
}