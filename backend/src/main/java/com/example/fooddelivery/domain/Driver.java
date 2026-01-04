package com.example.fooddelivery.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "drivers")
public class Driver {
    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    public Driver() {}

    public Driver(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
