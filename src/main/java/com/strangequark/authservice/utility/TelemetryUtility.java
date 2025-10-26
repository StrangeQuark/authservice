// Integration file: Telemetry

package com.strangequark.authservice.utility;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Service
public class TelemetryUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryUtility.class);

    /**
     * {@link AuthUtility} for authenticating the service account
     */
    @Autowired
    private AuthUtility authUtility;

    public void sendTelemetryEvent(String eventType, UUID userId, Map<String, Object> metadata) {
        LOGGER.info("Attempting to post message to auth telemetry Kafka topic");

        String accessToken = authUtility.authenticateServiceAccount();
        accessToken = "Bearer " + accessToken;

        JSONObject requestBody = new JSONObject();
        requestBody.put("serviceName", "authservice");
        requestBody.put("eventType", eventType);
        requestBody.put("userId", userId);
        requestBody.put("timestamp", LocalDateTime.now());
        requestBody.put("metadata", metadata);

        Properties props = new Properties();
        props.put("bootstrap.servers", "telemetry-kafka:9093");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        String topic = "auth-telemetry-events";

        LOGGER.info("Message created, attempting to post to auth telemetry Kafka topic");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(
                topic,
                null,
                null,
                requestBody.toString(),
                List.of(new RecordHeader("Authorization", accessToken.getBytes())));
        producer.send(record);
        producer.close();
    }
}
