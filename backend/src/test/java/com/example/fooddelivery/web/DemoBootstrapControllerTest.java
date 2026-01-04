package com.example.fooddelivery.web;

import com.example.fooddelivery.domain.Driver;
import com.example.fooddelivery.domain.MenuItem;
import com.example.fooddelivery.domain.Order;
import com.example.fooddelivery.domain.Restaurant;
import com.example.fooddelivery.dto.CreateOrderRequest;
import com.example.fooddelivery.repository.DriverRepository;
import com.example.fooddelivery.repository.MenuItemRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.example.fooddelivery.service.MenuService;
import com.example.fooddelivery.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DemoBootstrapControllerTest {

    private RestaurantRepository restaurantRepository;
    private MenuItemRepository menuItemRepository;
    private DriverRepository driverRepository;
    private OrderService orderService;
    private MenuService menuService;

    private DemoBootstrapController controller;

    @BeforeEach
    void setup() {
        restaurantRepository = mock(RestaurantRepository.class);
        menuItemRepository = mock(MenuItemRepository.class);
        driverRepository = mock(DriverRepository.class);
        orderService = mock(OrderService.class);
        menuService = mock(MenuService.class);

        controller = new DemoBootstrapController(
                restaurantRepository,
                menuItemRepository,
                driverRepository,
                orderService,
                menuService
        );
    }

    @Test
    void bootstrap_shouldCreateConfiguredCountsAndReturnSummary() {
        DemoBootstrapController.BootstrapRequest req = new DemoBootstrapController.BootstrapRequest();
        req.drivers = 2;
        req.restaurants = 3;
        req.orders = 4;

        // restaurants
        for (int i = 0; i < req.restaurants; i++) {
            Restaurant r = new Restaurant("R" + i);
            r.setId((long) (i + 1));
            when(restaurantRepository.save(any(Restaurant.class))).thenReturn(r);
        }

        when(restaurantRepository.count()).thenReturn((long) req.restaurants);
        when(driverRepository.count()).thenReturn((long) req.drivers);

        // drivers
        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation -> {
                    Driver d = invocation.getArgument(0);
                    d.setId(1L);
                    return d;
                });

        // orders
        when(orderService.create(any(CreateOrderRequest.class)))
                .thenAnswer(invocation -> {
                    CreateOrderRequest c = invocation.getArgument(0);
                    Restaurant r = new Restaurant("R");
                    r.setId(c.getRestaurantId());
                    Order o = new Order(r, c.getCustomerLat(), c.getCustomerLng());
                    o.setId(1L);
                    return o;
                });

        Map<String, Object> result = controller.bootstrap(req);

        assertEquals(req.drivers, result.get("driversCreated"));
        assertEquals(req.restaurants, result.get("restaurantsCreated"));
        assertEquals(req.orders, result.get("ordersCreated"));

        verify(restaurantRepository, times(req.restaurants)).save(any(Restaurant.class));
        verify(driverRepository, times(req.drivers)).save(any(Driver.class));
        verify(orderService, times(req.orders)).create(any(CreateOrderRequest.class));
    }
}