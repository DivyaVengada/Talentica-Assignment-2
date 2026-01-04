package com.example.fooddelivery.service;

import com.example.fooddelivery.domain.Order;
import com.example.fooddelivery.domain.OrderState;
import com.example.fooddelivery.dto.DriverLocationEvent;
import com.example.fooddelivery.dto.TrackingResponse;
import com.example.fooddelivery.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class DriverLocationConsumer {
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redis;
    private final OrderRepository orderRepository;

    public DriverLocationConsumer(ObjectMapper objectMapper,
                                  StringRedisTemplate redis,
                                  OrderRepository orderRepository) {
        this.objectMapper = objectMapper;
        this.redis = redis;
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "${app.kafka.topics.driverLocation}")
    public void onMessage(String message) {
        try {
            DriverLocationEvent evt = objectMapper.readValue(message, DriverLocationEvent.class);

            // idempotency (optional) - drop duplicates quickly
            if (evt.getEventId() != null && !evt.getEventId().isBlank()) {
                String dedupeKey = "dedupe:gps:" + evt.getEventId();
                Boolean ok = redis.opsForValue().setIfAbsent(dedupeKey, "1", Duration.ofMinutes(2));
                if (ok != null && !ok) {
                    return;
                }
            }

            // store latest location per driver
            String driverKey = "driver:" + evt.getDriverId() + ":latest";
            redis.opsForValue().set(driverKey, message, Duration.ofMinutes(10));

            // update tracking views for active orders of the driver
            List<Order> activeOrders = orderRepository.findByDriverId(evt.getDriverId());
            for (Order o : activeOrders) {
                // only show tracking after pickup
                if (o.getState() == OrderState.PICKED_UP || o.getState() == OrderState.OUT_FOR_DELIVERY) {
                    int stopsBeforeYou = estimateStopsBeforeYou(o.getId(), evt.getDriverId());
                    String msg = (stopsBeforeYou > 0) ? (stopsBeforeYou + " stop(s) before you") : null;

                    // Demo ETA: simple constant ranges. In real system, compute from routing engine/maps.
                    Integer etaMin = 10 * 60 + (stopsBeforeYou * 3 * 60);
                    Integer etaMax = etaMin + 6 * 60;

                    TrackingResponse view = new TrackingResponse(
                            o.getId(), o.getState().name(), evt.getDriverId(),
                            evt.getLat(), evt.getLng(), evt.getTimestamp(),
                            etaMin, etaMax,
                            stopsBeforeYou,
                            (stopsBeforeYou > 0) ? "Driver is completing another delivery nearby." : null,
                            null
                    );

                    String orderKey = "order:" + o.getId() + ":tracking_view";
                    redis.opsForValue().set(orderKey, objectMapper.writeValueAsString(view), Duration.ofMinutes(30));
                }
            }

        } catch (Exception e) {
            // In production: send to DLQ / error topic
        }
    }

    private int estimateStopsBeforeYou(Long orderId, Long driverId) {
        // Demo logic: orderId modulo 2 to simulate batching.
        // Real system: dispatch service supplies route plan.
        return (orderId % 2 == 0) ? 1 : 0;
    }
}
