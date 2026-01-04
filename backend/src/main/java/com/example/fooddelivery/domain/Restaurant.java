package com.example.fooddelivery.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_open", nullable = false)
    private boolean open = true;

    public Restaurant() {}

    public Restaurant(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public boolean isOpen() { return open; }

    public void setName(String name) { this.name = name; }
    public void setOpen(boolean open) { this.open = open; }

    public void setId(Long id) {
        this.id = id;
    }

}
