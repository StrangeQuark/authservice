package com.strangequark.authservice.utility;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class EmailUtility {
    public static void sendEmail(String recipient, String subject) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("recipient", recipient);
        requestBody.put("sender", "donotreply@authservice.com");
        requestBody.put("subject", subject);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

        new RestTemplate().postForObject("http://localhost:6005/email/sendPasswordResetEmail", requestEntity, String.class);
    }
}
