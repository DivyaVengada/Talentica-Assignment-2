package com.example.fooddelivery.repository;

import com.example.fooddelivery.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByDriverId(Long driverId);
}
