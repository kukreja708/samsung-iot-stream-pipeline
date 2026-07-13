package com.samsung.iot.ingestion_service;

import com.samsung.iot.ingestion_service.entity.DeviceTelemetryEntity;
import com.samsung.iot.ingestion_service.repository.TelemetryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TelemetryConsumerService {

    private final TelemetryRepository telemetryRepository;
    public TelemetryConsumerService(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    @KafkaListener(
            topics = "device-telemetry",
            groupId = "samsung-analytics-group",
            properties = {"spring.json.value.default.type=com.samsung.iot.ingestion_service.DeviceTelemetry"}
    )
    public void consumeTelemetry(DeviceTelemetry telemetry, Acknowledgment ack) {
        // 1. Parse metrics map safely
        Double cpuUsage = Double.valueOf(telemetry.getMetrics().get("cpuUsagePercent").toString());
        Double temperature = Double.valueOf(telemetry.getMetrics().get("temperatureCelsius").toString());
        String status = telemetry.getMetrics().get("status").toString();

        System.out.println("📥 [KAFKA CONSUMED] -> Device: " + telemetry.getDeviceId() + " | Temp: " + temperature + "°C | CPU: " + cpuUsage + "%");

        // 2. Real-Time Anomaly Detection Rules
        if (cpuUsage > 80.0) {
            System.err.println("🚨 [CRITICAL ALERT] Device " + telemetry.getDeviceId() + " is OVERHEATING! CPU: " + cpuUsage + "%");
        }

        if ("MAINTENANCE".equals(status)) {
            System.out.println("⚠️ [WARNING] Device " + telemetry.getDeviceId() + " requires maintenance attention.");
        }

        DeviceTelemetryEntity entity = new DeviceTelemetryEntity();
        entity.setDeviceId(telemetry.getDeviceId());
        entity.setCpuUsage(cpuUsage);
        entity.setRecordedAt(Instant.parse(telemetry.getTimestamp()));
        entity.setStatus(status);
        entity.setTemperature(temperature);

        try {
            // By forcing .block(), we tie the Kafka message acknowledgment directly to the DB write thread.
            // If the DB is down, .block() will throw a real runtime exception out of this method!
            DeviceTelemetryEntity savedItem = telemetryRepository.save(entity).block();

            System.out.println("💾 [DATABASE SAVED] -> Primary Key: " + savedItem.getId());
            ack.acknowledge(); // Only commits if the block operation succeeds cleanly

        } catch (Exception e) {
            System.err.println("❌ Database write failed! Escalating exception to Kafka Error Handler...");
            throw e; // This raw exception triggers your KafkaConsumerConfig retry loop!
        }
    }
}
