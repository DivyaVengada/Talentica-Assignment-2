package com.example.fooddelivery.service;

import com.example.fooddelivery.domain.MenuItem;
import com.example.fooddelivery.domain.Restaurant;
import com.example.fooddelivery.dto.MenuResponse;
import com.example.fooddelivery.repository.MenuItemRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public MenuService(RestaurantRepository restaurantRepository,
                       MenuItemRepository menuItemRepository,
                       StringRedisTemplate redis,
                       ObjectMapper objectMapper) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public MenuResponse getMenu(Long restaurantId) {
        String key = "restaurant:" + restaurantId + ":menu_plus_status";
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, MenuResponse.class);
            } catch (Exception ignored) {
                // fall through to rebuild
            }
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        List<MenuItem> items = menuItemRepository.findByRestaurantId(restaurantId);

        List<MenuResponse.MenuItemDto> dtos = items.stream()
                .map(i -> new MenuResponse.MenuItemDto(i.getId(), i.getName(), i.getPrice()))
                .collect(Collectors.toList());

        MenuResponse response = new MenuResponse(restaurant.getId(), restaurant.getName(), restaurant.isOpen(), dtos);

        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(response), Duration.ofMinutes(5));
        } catch (Exception ignored) {}

        return response;
    }

    public void invalidateMenuCache(Long restaurantId) {
        redis.delete("restaurant:" + restaurantId + ":menu_plus_status");
    }
}
