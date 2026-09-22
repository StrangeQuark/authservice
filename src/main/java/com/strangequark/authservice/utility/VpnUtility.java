package com.strangequark.authservice.utility;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class VpnUtility {
    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<?> revokeUserDevices(UUID userId, String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("userId", userId);

        return restTemplate.exchange(
                "http://vpn-service:6040/api/vpn/revoke-user-devices",
                HttpMethod.POST,
                new HttpEntity<>(requestBody.toString(), headers),
                String.class
        );
    }
}
