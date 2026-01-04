package com.example.fooddelivery.web;

import com.example.fooddelivery.dto.MenuResponse;
import com.example.fooddelivery.service.MenuService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final MenuService menuService;

    public RestaurantController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/{restaurantId}/menu")
    public MenuResponse getMenu(@PathVariable Long restaurantId) {
        return menuService.getMenu(restaurantId);
    }
}
