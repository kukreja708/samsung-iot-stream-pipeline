package com.samsung.iot.ingestion_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    // 1. Mark it as a Spring Bean so the container factory is registered globally
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // 2. Configure our retry policy: 3 total attempts, waiting 2000ms (2 seconds) between each backoff loop
        FixedBackOff backOff = new FixedBackOff(2000L, 2);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {
            // This is the Dead Letter recovery callback! It triggers if all 3 attempts fail.
            System.err.println("🚨 [DLQ TRIGGERED] Message failed completely after all retries. " +
                    "Offset: " + record.offset() + " | Error: " + exception.getMessage());
            // In a production app, we would write code here to route this record to a "device-telemetry-dlq" topic.
        }, backOff);

        factory.setCommonErrorHandler(errorHandler);

        // 3. Force manual acknowledgment mode at the engine container level
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
