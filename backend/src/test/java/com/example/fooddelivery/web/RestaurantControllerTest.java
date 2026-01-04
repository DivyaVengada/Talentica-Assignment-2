package com.example.fooddelivery.web;

import com.example.fooddelivery.dto.MenuResponse;
import com.example.fooddelivery.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RestaurantControllerTest {

    private MenuService menuService;
    private RestaurantController controller;

    @BeforeEach
    void setup() {
        menuService = mock(MenuService.class);
        controller = new RestaurantController(menuService);
    }

    @Test
    void getMenu_shouldDelegateToService() {
        MenuResponse resp = new MenuResponse(1L, "R1", true, java.util.List.of());
        when(menuService.getMenu(1L)).thenReturn(resp);

        MenuResponse result = controller.getMenu(1L);

        assertEquals(1L, result.getRestaurantId());
        verify(menuService).getMenu(1L);
    }
}