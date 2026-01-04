package com.example.fooddelivery.web;

import com.example.fooddelivery.dto.DriverLocationEvent;
import com.example.fooddelivery.dto.DriverLocationUpdate;
import com.example.fooddelivery.service.DriverLocationProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DriverControllerTest {

    private DriverLocationProducer producer;
    private DriverController controller;

    @BeforeEach
    void setup() {
        producer = mock(DriverLocationProducer.class);
        controller = new DriverController(producer);
    }

    @Test
    void ingest_shouldMapDtoAndSendToProducer() {
        DriverLocationUpdate update = new DriverLocationUpdate();
        update.setEventId("evt-1");
        update.setLat(12.9);
        update.setLng(77.6);
        update.setHeading(90);
        update.setSpeedMps(5.5);
        update.setTimestamp(Instant.now());

        controller.ingest(10L, update);

        verify(producer).publish(any(DriverLocationEvent.class));
    }
}