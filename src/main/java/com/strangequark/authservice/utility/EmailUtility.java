// Integration file: Email

package com.strangequark.authservice.utility;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

/**
 * Utility for sending API requests to the EmailService
 */
public class EmailUtility {
    /**
     * {@link Logger} for writing {@link EmailUtility} application logs
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtility.class);

    /**
     * Sender address for auth service emails
     */
    private static String SENDER = "donotreply@authservice.com";

    /**
     * Business logic sending an API request to the EmailService
     * @param recipient
     * @param subject
     * @param emailType
     */
    public static ResponseEntity<?> sendEmail(String recipient, String subject, EmailType emailType) {
        LOGGER.info("Attempting to send email API request");

        //Set the headers
        LOGGER.info("Setting email API request headers");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Create the request body
        LOGGER.info("Creating email API request body");
        JSONObject requestBody = new JSONObject();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", SENDER);
        requestBody.put("subject", subject);

        //Compile the HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

        //Check the email type, and send to the correct EmailService endpoint accordingly
        String url = "";
        switch (emailType) {
            case REGISTER -> url = "http://email-service:6005/api/email/send-register-email";
            case PASSWORD_RESET -> url = "http://email-service:6005/api/email/send-password-reset-email";
        }

        LOGGER.info("Email API request creation complete, attempting to send request");
        return new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
    }

    public static void sendAsyncEmail(String recipient, String subject, EmailType emailType) {
        LOGGER.info("Attempting to post message to email Kafka topic");

        Properties props = new Properties();
        props.put("bootstrap.servers", "email-kafka:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        String topic = "";

        switch (emailType) {
            case REGISTER -> topic = "register-email-events";
            case PASSWORD_RESET -> topic = "password-reset-email-events";
        }

        LOGGER.info("Message created, attempting to post to email Kafka topic");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<>(topic, "{\"recipient\":\"" + recipient + "\",\"sender\":\"" + SENDER +
                "\",\"email\":\"\",\"subject\":\"" + subject + "\"}"));
        producer.close();
    }
}
