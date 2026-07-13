package com.samsung.iot.ingestion_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class StreamConfig {

    @Bean
    public Sinks.Many<DeviceTelemetry> telemetrySink() {
        // Creates a single, global, replayable broadcast sink across all threads
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}
