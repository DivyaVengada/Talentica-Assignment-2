package com.example.fooddelivery.service;

import com.example.fooddelivery.domain.Order;
import com.example.fooddelivery.domain.OrderState;
import com.example.fooddelivery.dto.DriverLocationEvent;
import com.example.fooddelivery.dto.TrackingResponse;
import com.example.fooddelivery.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DriverLocationConsumerTest {

    private ObjectMapper objectMapper;
    private StringRedisTemplate redis;
    private ValueOperations<String, String> valueOps;
    private OrderRepository orderRepository;

    private DriverLocationConsumer consumer;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        redis = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

        orderRepository = mock(OrderRepository.class);

        consumer = new DriverLocationConsumer(objectMapper, redis, orderRepository);
    }

    @Test
    void onMessage_shouldIgnoreDuplicateEventId() throws Exception {
        DriverLocationEvent evt = new DriverLocationEvent(
                10L, "evt-1", 12.9, 77.6, 90, 5.5, Instant.now()
        );
        String json = objectMapper.writeValueAsString(evt);

        when(valueOps.setIfAbsent(eq("dedupe:gps:evt-1"), eq("1"), any(Duration.class)))
                .thenReturn(false);

        consumer.onMessage(json);

        verify(orderRepository, never()).findByDriverId(anyLong());
        verify(valueOps, never()).set(startsWith("order:"), anyString(), any());
    }

    @Test
    void onMessage_shouldWriteTrackingForPickedUpAndOutForDeliveryOrders() throws Exception {
        DriverLocationEvent evt = new DriverLocationEvent(
                10L, "evt-2", 12.9, 77.6, 90, 5.5, Instant.now()
        );
        String json = objectMapper.writeValueAsString(evt);

        when(valueOps.setIfAbsent(eq("dedupe:gps:evt-2"), eq("1"), any(Duration.class)))
                .thenReturn(true);

        Order pickedUp = new Order();
        pickedUp.setId(100L);
        pickedUp.setState(OrderState.PICKED_UP);

        Order outForDelivery = new Order();
        outForDelivery.setId(101L);
        outForDelivery.setState(OrderState.OUT_FOR_DELIVERY);

        Order createdOnly = new Order();
        createdOnly.setId(102L);
        createdOnly.setState(OrderState.CREATED);

        when(orderRepository.findByDriverId(10L))
                .thenReturn(List.of(pickedUp, outForDelivery, createdOnly));

        consumer.onMessage(json);

        // driver latest should be set
        verify(valueOps).set(eq("driver:10:latest"), eq(json), any(Duration.class));

        // tracking views should be set only for pickedUp & outForDelivery
        verify(valueOps).set(startsWith("order:100:tracking_view"), anyString(), any(Duration.class));
        verify(valueOps).set(startsWith("order:101:tracking_view"), anyString(), any(Duration.class));
        verify(valueOps, never()).set(startsWith("order:102:tracking_view"), anyString(), any());

        // optionally capture one payload and decode to ensure shape
        // (light sanity check)
        // ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        // verify(valueOps).set(eq("order:100:tracking_view"), payloadCaptor.capture(), any(Duration.class));
        // TrackingResponse tr = objectMapper.readValue(payloadCaptor.getValue(), TrackingResponse.class);
        // assertEquals(100L, tr.getOrderId());
        // assertEquals("PICKED_UP", tr.getState());
    }

    @Test
    void onMessage_shouldHandleEventWithoutEventId() throws Exception {
        DriverLocationEvent evt = new DriverLocationEvent(
                10L, null, 12.9, 77.6, 90, 5.5, Instant.now()
        );
        String json = objectMapper.writeValueAsString(evt);

        Order pickedUp = new Order();
        pickedUp.setId(200L);
        pickedUp.setState(OrderState.PICKED_UP);

        when(orderRepository.findByDriverId(10L)).thenReturn(List.of(pickedUp));

        consumer.onMessage(json);

        // should NOT attempt dedupe when eventId is null
        verify(valueOps, never()).setIfAbsent(startsWith("dedupe:gps:"), anyString(), any());

        // but should still update driver:latest and order tracking
        verify(valueOps).set(eq("driver:10:latest"), eq(json), any(Duration.class));
        verify(valueOps).set(startsWith("order:200:tracking_view"), anyString(), any(Duration.class));
    }
}