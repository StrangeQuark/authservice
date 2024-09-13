package com.strangequark.authservice.utility;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Utility for sending API requests to the EmailService
 */
public class EmailUtility {

    /**
     * Business logic sending an API request to the EmailService
     * @param recipient
     * @param subject
     * @param emailType
     */
    public static void sendEmail(String recipient, String subject, EmailType emailType) {
        //Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Create the request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", "donotreply@authservice.com");
        requestBody.put("subject", subject);

        //Compile the HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

        //Check the email type, and send to the correct EmailService endpoint accordingly
        String url = "";
        switch (emailType) {
            case REGISTER -> url = "http://localhost:6005/email/sendRegisterEmail";
            case PASSWORD_RESET -> url = "http://localhost:6005/email/sendPasswordResetEmail";
        }
        new RestTemplate().postForObject(url, requestEntity, String.class);
    }
}
