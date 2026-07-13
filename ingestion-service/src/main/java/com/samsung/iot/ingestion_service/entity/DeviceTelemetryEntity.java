package com.samsung.iot.ingestion_service.entity; // Kept in your core package space

import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;

@Table(name="device_telemetry")
public class DeviceTelemetryEntity implements Persistable<String> {

    @Column("device_id")
    private String deviceId;

    @Column("recorded_at")
    private Instant recordedAt;

    @Column("temperature")
    private Double temperature;

    @Column("cpu_usage")
    private Double cpuUsage;

    @Column("status")
    private String status;

    // Standard Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // --- Persistable Interface Methods Required for R2DBC ---

    @Override
    @Transient // Tells Spring to ignore this method for SQL columns
    public String getId() {
        return this.deviceId + "_" + this.recordedAt; // Creates a virtual combined lookup ID
    }

    @Override
    @Transient
    public boolean isNew() {
        return true; // Forces R2DBC to always run an INSERT command instead of an UPDATE statement
    }
}
