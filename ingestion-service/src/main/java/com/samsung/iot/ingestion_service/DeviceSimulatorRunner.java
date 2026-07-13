package com.samsung.iot.ingestion_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class DeviceSimulatorRunner implements CommandLineRunner {

    private final WebClient webClient = WebClient.create("http://localhost:8080");
    private final Random random = new Random();
    private final List<String> devices = List.of(
            "SAMSUNG-SMART-AC-001", "SAMSUNG-SMART-FRIDGE-002",
            "SAMSUNG-SMART-TV-003", "SAMSUNG-SMART-WASHER-004"
    );

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Starting Native Java IoT Device Simulator Stream...");

        // Delay start by 5 seconds to ensure the Netty server is completely booted up first
        Flux.interval(Duration.ofMillis(100))
                .delaySubscription(Duration.ofSeconds(5))
                .publishOn(Schedulers.boundedElastic())
                .subscribe(tick -> {
                    try {
                        DeviceTelemetry payload = generateMockTelemetry();

                        webClient.post()
                                .uri("/api/v1/telemetry")
                                .bodyValue(payload)
                                .retrieve()
                                .bodyToMono(String.class)
                                .subscribe(
                                        response -> System.out.println("[SENT] Device: " + payload.getDeviceId() + " | Response: " + response),
                                        error -> System.err.println("⚠️ Server returned an error: " + error.getMessage()) // Safely catches 500s!
                                );

                    } catch (Exception e) {
                        System.err.println("❌ Simulator stream error: " + e.getMessage());
                    }
                });
    }

    private DeviceTelemetry generateMockTelemetry() {
        DeviceTelemetry telemetry = new DeviceTelemetry();
        telemetry.setDeviceId(devices.get(random.nextInt(devices.size())));
        telemetry.setTimestamp(java.time.Instant.now().toString()); // Converted directly to safe String text
        telemetry.setMetrics(Map.of(
                "temperatureCelsius", Math.round((16.0 + (10.0 * random.nextDouble())) * 100.0) / 100.0,
                "cpuUsagePercent", Math.round((5.0 + (80.0 * random.nextDouble())) * 100.0) / 100.0,
                "status", random.nextBoolean() ? "ONLINE" : "MAINTENANCE"
        ));
        return telemetry;
    }

}
