package com.example.fooddelivery.dto;

import jdk.jfr.DataAmount;

import java.util.List;


public class MenuResponse {
    public MenuResponse() {
    }

    public static class MenuItemDto {
        private Long itemId;
        private String name;
        private double price;

        public MenuItemDto() {
        }

        public MenuItemDto(Long itemId, String name, double price) {
            this.itemId = itemId;
            this.name = name;
            this.price = price;
        }

        public Long getItemId() { return itemId; }
        public String getName() { return name; }
        public double getPrice() { return price; }
    }

    private Long restaurantId;
    private String restaurantName;
    private boolean open;
    private List<MenuItemDto> menuItems;

    public MenuResponse(Long restaurantId, String restaurantName, boolean open, List<MenuItemDto> menuItems) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.open = open;
        this.menuItems = menuItems;
    }

    public Long getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public boolean isOpen() { return open; }
    public List<MenuItemDto> getMenuItems() { return menuItems; }
}
