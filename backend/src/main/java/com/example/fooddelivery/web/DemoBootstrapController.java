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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/internal/demo")
public class DemoBootstrapController {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final DriverRepository driverRepository;
    private final OrderService orderService;
    private final MenuService menuService;

    public DemoBootstrapController(RestaurantRepository restaurantRepository,
                                  MenuItemRepository menuItemRepository,
                                  DriverRepository driverRepository,
                                  OrderService orderService,
                                  MenuService menuService) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.driverRepository = driverRepository;
        this.orderService = orderService;
        this.menuService = menuService;
    }

    public static class BootstrapRequest {
        public int drivers = 50;
        public int restaurants = 10;
        public int orders = 50;
    }

    @PostMapping("/bootstrap")
    public Map<String, Object> bootstrap(@RequestBody BootstrapRequest request) {
        Random rnd = new Random(42);

        for (int i = 0; i < request.restaurants; i++) {
            Restaurant r = restaurantRepository.save(new Restaurant("Restaurant " + (i + 1)));
            // add 8 menu items each
            for (int j = 0; j < 8; j++) {
                menuItemRepository.save(new MenuItem(r, "Item " + (j + 1), 50 + rnd.nextInt(200)));
            }
            menuService.invalidateMenuCache(r.getId());
        }

        for (int i = 0; i < request.drivers; i++) {
            driverRepository.save(new Driver("Driver " + (i + 1)));
        }

        long restaurantCount = restaurantRepository.count();
        long driverCount = driverRepository.count();

        for (int i = 0; i < request.orders; i++) {
            long restaurantId = 1 + (i % Math.max(1, restaurantCount));
            long driverId = 1 + (i % Math.max(1, driverCount));

            CreateOrderRequest c = new CreateOrderRequest();
            c.setRestaurantId(restaurantId);
            // random customer point around Bangalore-ish area
            c.setCustomerLat(12.90 + rnd.nextDouble() * 0.10);
            c.setCustomerLng(77.55 + rnd.nextDouble() * 0.15);

            Order o = orderService.create(c);
            orderService.assignDriver(o.getId(), driverId);
            orderService.markPickedUp(o.getId());
        }

        Map<String, Object> out = new HashMap<>();
        out.put("driversCreated", request.drivers);
        out.put("restaurantsCreated", request.restaurants);
        out.put("ordersCreated", request.orders);
        return out;
    }
}
