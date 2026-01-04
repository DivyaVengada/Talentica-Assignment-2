package com.example.fooddelivery.service;

import com.example.fooddelivery.domain.Driver;
import com.example.fooddelivery.domain.Order;
import com.example.fooddelivery.domain.OrderState;
import com.example.fooddelivery.dto.TrackingResponse;
import com.example.fooddelivery.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrackingServiceTest {

    private StringRedisTemplate redis;
    private ValueOperations<String, String> valueOps;
    private OrderRepository orderRepository;
    private ObjectMapper objectMapper;

    private TrackingService trackingService;

    @BeforeEach
    void setup() {
        redis = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        orderRepository = mock(OrderRepository.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        when(redis.opsForValue()).thenReturn(valueOps);

        trackingService = new TrackingService(
                redis,
                orderRepository,
                objectMapper,
                15   // staleSeconds
        );
    }

    // ---------------------------------------------------------
    // 1. CACHE HIT — WITH TIMESTAMP → FRESHNESS CALCULATED
    // ---------------------------------------------------------
    @Test
    void testGetTracking_cacheHit_withTimestamp() throws Exception {
        Instant ts = Instant.now().minusSeconds(5);

        TrackingResponse cached = new TrackingResponse(
                1L, "PICKED_UP", 10L,
                12.9, 77.6,
                ts,
                300, 600,
                1,
                "Your order is on the way",
                null
        );

        String json = objectMapper.writeValueAsString(cached);
        when(valueOps.get("order:1:tracking_view")).thenReturn(json);

        TrackingResponse result = trackingService.getTracking(1L);

        assertEquals(1L, result.getOrderId());
        assertEquals("PICKED_UP", result.getState());
        assertNotNull(result.getFreshnessSeconds());
        assertTrue(result.getFreshnessSeconds() >= 4 && result.getFreshnessSeconds() <= 6);
    }

    // ---------------------------------------------------------
    // 2. CACHE HIT — NO TIMESTAMP → RETURN AS-IS
    // ---------------------------------------------------------
    @Test
    void testGetTracking_cacheHit_noTimestamp() throws Exception {
        TrackingResponse cached = new TrackingResponse(
                1L, "ASSIGNED", 10L,
                12.9, 77.6,
                null,   // no timestamp
                null, null, null,
                null,
                null
        );

        String json = objectMapper.writeValueAsString(cached);
        when(valueOps.get("order:1:tracking_view")).thenReturn(json);

        TrackingResponse result = trackingService.getTracking(1L);

        assertEquals("ASSIGNED", result.getState());
        assertNull(result.getFreshnessSeconds());
    }

    // ---------------------------------------------------------
    // 3. CACHE MISS → LOAD MINIMAL INFO FROM DB
    // ---------------------------------------------------------
    @Test
    void testGetTracking_cacheMiss_loadsFromDb() {
        when(valueOps.get("order:1:tracking_view")).thenReturn(null);

        Order order = new Order();
        order.setId(1L);
        order.setState(OrderState.PICKED_UP);

        Driver d = new Driver("Driver1");
        d.setId(10L);
        order.setDriver(d);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        TrackingResponse result = trackingService.getTracking(1L);

        assertEquals(1L, result.getOrderId());
        assertEquals("PICKED_UP", result.getState());
        assertEquals(10L, result.getDriverId());
        assertNull(result.getDriverLat());
        assertNull(result.getDriverLng());
    }

    // ---------------------------------------------------------
    // 4. ORDER NOT FOUND
    // ---------------------------------------------------------
    @Test
    void testGetTracking_orderNotFound() {
        when(valueOps.get("order:1:tracking_view")).thenReturn(null);
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trackingService.getTracking(1L));
    }

    // ---------------------------------------------------------
    // 5. STALE SECONDS PROPERTY
    // ---------------------------------------------------------
    @Test
    void testGetStaleSeconds() {
        assertEquals(15, trackingService.getStaleSeconds());
    }
}