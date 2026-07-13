package com.samsung.iot.ingestion_service;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic telemetryTopic() {
        return TopicBuilder.name("device-telemetry")
                .partitions(3) // Splits data streams across 3 virtual channels for high throughput
                .replicas(1)   // Keeps 1 copy locally
                .build();
    }
}
