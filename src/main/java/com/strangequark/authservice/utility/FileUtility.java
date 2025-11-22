// Integration file: File

package com.strangequark.authservice.utility;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Utility for sending API requests to the FileService
 */
@Service
public class FileUtility {
    /**
     * {@link Logger} for writing {@link FileUtility} application logs
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtility.class);

    /**
     * Business logic sending an API request to the FileService
     * @param username User to be deleted
     */
    public ResponseEntity<?> deleteUserFromAllCollections(String username, String authToken) {
        LOGGER.debug("Attempting to send file API request");

        //Set the headers
        LOGGER.debug("Setting file API request headers");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Create the request body
        LOGGER.debug("Creating file API request body");
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", username);

        //Compile the HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

        //Define the endpoint URL
        String url = "http://file-service:6010/api/file/delete-user-from-all-collections";

        LOGGER.debug("File API request creation complete, attempting to send request");
        return new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
    }
}
