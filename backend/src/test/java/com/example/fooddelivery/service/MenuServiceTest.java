package com.example.fooddelivery.service;

import com.example.fooddelivery.domain.MenuItem;
import com.example.fooddelivery.domain.Restaurant;
import com.example.fooddelivery.dto.MenuResponse;
import com.example.fooddelivery.repository.MenuItemRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuServiceTest {

    private RestaurantRepository restaurantRepository;
    private MenuItemRepository menuItemRepository;
    private StringRedisTemplate redis;
    private ValueOperations<String, String> valueOps;
    private ObjectMapper objectMapper;

    private MenuService menuService;

    @BeforeEach
    void setup() {
        restaurantRepository = mock(RestaurantRepository.class);
        menuItemRepository = mock(MenuItemRepository.class);
        redis = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        objectMapper = new ObjectMapper();

        when(redis.opsForValue()).thenReturn(valueOps);

        menuService = new MenuService(
                restaurantRepository,
                menuItemRepository,
                redis,
                objectMapper
        );
    }

    // ---------------------------------------------------------
    // 1. CACHE HIT
    // ---------------------------------------------------------
    @Test
    void testGetMenu_cacheHit() throws Exception {
        MenuResponse cached = new MenuResponse(1L, "R1", true, List.of());
        String json = objectMapper.writeValueAsString(cached);

        when(valueOps.get("restaurant:1:menu_plus_status")).thenReturn(json);

        MenuResponse result = menuService.getMenu(1L);

        assertEquals("R1", result.getRestaurantName());
        verifyNoInteractions(restaurantRepository);
        verifyNoInteractions(menuItemRepository);
    }

    // ---------------------------------------------------------
    // 2. CACHE MISS → DB FETCH + REDIS WRITE
    // ---------------------------------------------------------
    @Test
    void testGetMenu_cacheMiss_buildsMenu() throws Exception {
        when(valueOps.get("restaurant:1:menu_plus_status")).thenReturn(null);

        Restaurant r = new Restaurant("R1");
        r.setId(1L);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(r));

        MenuItem item = new MenuItem(r, "Item1", 100);
        item.setId(10L);

        when(menuItemRepository.findByRestaurantId(1L))
                .thenReturn(List.of(item));

        MenuResponse result = menuService.getMenu(1L);

        assertEquals("R1", result.getRestaurantName());
        assertEquals(1, result.getMenuItems().size());
        assertEquals("Item1", result.getMenuItems().get(0).getName());

        // verify redis write
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOps).set(
                keyCaptor.capture(),
                anyString(),
                any()
        );

        assertEquals("restaurant:1:menu_plus_status", keyCaptor.getValue());
    }

    // ---------------------------------------------------------
    // 3. RESTAURANT NOT FOUND
    // ---------------------------------------------------------
    @Test
    void testGetMenu_restaurantNotFound() {
        when(valueOps.get("restaurant:1:menu_plus_status")).thenReturn(null);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> menuService.getMenu(1L));
    }

    // ---------------------------------------------------------
    // 4. INVALIDATE CACHE
    // ---------------------------------------------------------
    @Test
    void testInvalidateMenuCache() {
        menuService.invalidateMenuCache(5L);

        verify(redis).delete("restaurant:5:menu_plus_status");
    }
}