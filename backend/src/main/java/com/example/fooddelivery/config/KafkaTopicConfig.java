package com.example.fooddelivery.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic driverLocationTopic(@Value("${app.kafka.topics.driverLocation}") String topicName) {
        // For local dev: 3 partitions gives parallelism headroom; replication=1 in single-broker compose.
        return new NewTopic(topicName, 3, (short) 1);
    }
}
