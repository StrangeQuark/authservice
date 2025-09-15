// Integration file: File

package com.strangequark.authservice.utility;

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
     * @param id User ID to be deleted
     */
    public ResponseEntity<?> deleteUserByIdFromAllCollections(String id, String authToken) {
        LOGGER.info("Attempting to send file API request");

        //Set the headers
        LOGGER.info("Setting file API request headers");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.TEXT_PLAIN);

        //Compile the HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(id, headers);

        //Define the endpoint URL
        String url = "http://file-service:6010/api/file/delete-user-by-id-from-all-collections";

        LOGGER.info("File API request creation complete, attempting to send request");
        return new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
    }
}
