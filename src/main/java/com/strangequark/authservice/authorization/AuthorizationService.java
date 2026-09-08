package com.strangequark.authservice.authorization;

import com.strangequark.authservice.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationService.class);

    private final AuthorizationRepository authorizationRepository;

    public AuthorizationService(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    public ResponseEntity<?> getAuthorizations() {
        LOGGER.info("Attempting to get authorizations");

        try {
            return ResponseEntity.ok(authorizationRepository.findAll());
        } catch(Exception ex) {
            LOGGER.error("Failed to get authorizations: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
