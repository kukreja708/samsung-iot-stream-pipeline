package com.samsung.iot.ingestion_service;

import java.util.Map;

public class DeviceTelemetry {
    private String deviceId;
    private String timestamp; // Changed from Instant to String
    private Map<String, Object> metrics;

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
}
