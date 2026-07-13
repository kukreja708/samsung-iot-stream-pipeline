package com.samsung.iot.ingestion_service.repository;

import com.samsung.iot.ingestion_service.entity.DeviceTelemetryEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelemetryRepository extends R2dbcRepository<DeviceTelemetryEntity, String> {


}
