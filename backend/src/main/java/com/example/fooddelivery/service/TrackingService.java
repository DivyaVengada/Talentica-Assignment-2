package com.example.fooddelivery.service;

import com.example.fooddelivery.domain.Order;
import com.example.fooddelivery.dto.TrackingResponse;
import com.example.fooddelivery.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TrackingService {
    private final StringRedisTemplate redis;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final int staleSeconds;

    public TrackingService(StringRedisTemplate redis,
                           OrderRepository orderRepository,
                           ObjectMapper objectMapper,
                           @Value("${app.tracking.staleSeconds:15}") int staleSeconds) {
        this.redis = redis;
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
        this.staleSeconds = staleSeconds;
    }

    public TrackingResponse getTracking(Long orderId) {
        String key = "order:" + orderId + ":tracking_view";
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            try {
                TrackingResponse r = objectMapper.readValue(cached, TrackingResponse.class);
                // freshness
                if (r.getLocationTimestamp() != null) {
                    long age = Math.max(0, Instant.now().getEpochSecond() - r.getLocationTimestamp().getEpochSecond());
                    return new TrackingResponse(r.getOrderId(), r.getState(), r.getDriverId(), r.getDriverLat(), r.getDriverLng(),
                            r.getLocationTimestamp(), r.getEtaMinSeconds(), r.getEtaMaxSeconds(), r.getBatchingStopsBeforeYou(),
                            r.getBatchingMessage(), (int) age);
                }
                return r;
            } catch (Exception ignored) {}
        }

        // If no cache, return minimal info from DB
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        Long driverId = (o.getDriver() == null) ? null : o.getDriver().getId();
        return new TrackingResponse(o.getId(), o.getState().name(), driverId,
                null, null, null, null, null, null,
                null, null);
    }

    public int getStaleSeconds() { return staleSeconds; }
}
