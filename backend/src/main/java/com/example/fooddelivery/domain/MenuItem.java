package com.example.fooddelivery.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_items")
public class MenuItem {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double price;

    public MenuItem() {}

    public MenuItem(Restaurant restaurant, String name, double price) {
        this.restaurant = restaurant;
        this.name = name;
        this.price = price;
    }

    public Long getId() { return id; }
    public Restaurant getRestaurant() { return restaurant; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }

    public void setId(Long id) {
        this.id = id;
    }
}
