package com.example.fooddelivery.web;

import com.example.fooddelivery.dto.DriverLocationEvent;
import com.example.fooddelivery.dto.DriverLocationUpdate;
import com.example.fooddelivery.service.DriverLocationProducer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    private final DriverLocationProducer producer;

    public DriverController(DriverLocationProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/{driverId}/location")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingest(@PathVariable Long driverId, @Valid @RequestBody DriverLocationUpdate update) {
        DriverLocationEvent evt = new DriverLocationEvent(
                driverId,
                update.getEventId(),
                update.getLat(),
                update.getLng(),
                update.getHeading(),
                update.getSpeedMps(),
                update.getTimestamp()
        );
        producer.publish(evt);
    }
}
