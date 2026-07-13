package com.samsung.iot.ingestion_service;

import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequestMapping("/api/v1/telemetry")
@CrossOrigin(origins = "http://localhost:5173")
public class TelemetryIngestionController {

    private final KafkaTemplate<String, DeviceTelemetry> kafkaTemplate;
    private static final String TOPIC_NAME = "device-telemetry";
    private final Sinks.Many<DeviceTelemetry> telemetrySink; // Injected globally

    // Constructor Injection
    public TelemetryIngestionController(KafkaTemplate<String, DeviceTelemetry> kafkaTemplate, Sinks.Many<DeviceTelemetry> telemetrySink) {
        this.kafkaTemplate = kafkaTemplate;
        this.telemetrySink = telemetrySink;
    }

    @PostMapping
    public Mono<String> ingestTelemetry(@RequestBody DeviceTelemetry telemetry) {
        return Mono.fromRunnable(() -> {
            // Asynchronously push telemetry payload to our local Kafka topic
            kafkaTemplate.send(TOPIC_NAME, telemetry.getDeviceId(), telemetry);
            telemetrySink.tryEmitNext(telemetry); // Emits directly into the shared singleton
        }).thenReturn("Telemetry event queued successfully");
    }

    // REPLACED: Added unbuffered event stream configuration
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DeviceTelemetry> streamTelemetry() {
        return telemetrySink.asFlux().log("stream-logger");
    }



}
