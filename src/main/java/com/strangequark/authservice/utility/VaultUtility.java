// Integration file: Vault

package com.strangequark.authservice.utility;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Utility for sending API requests to the VaultService
 */
@Service
public class VaultUtility {
    /**
     * {@link Logger} for writing {@link VaultUtility} application logs
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VaultUtility.class);

    /**
     * Business logic sending an API request to the VaultService
     * @param username User to be deleted
     */
    public ResponseEntity<?> deleteUserFromAllServices(String username, String authToken) {
        LOGGER.info("Attempting to send vault API request");

        //Set the headers
        LOGGER.info("Setting vault API request headers");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Create the request body
        LOGGER.info("Creating file API request body");
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", username);

        //Compile the HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

        //Define the endpoint URL
        String url = "http://vault-service:6020/api/vault/delete-user-from-all-services";

        LOGGER.info("Vault API request creation complete, attempting to send request");
        return new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
    }
}
