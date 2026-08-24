package com.strangequark.authservice.authorization;

import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.serviceaccount.ServiceAccountRepository;
import com.strangequark.authservice.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationService.class);

    private final AuthorizationRepository authorizationRepository;
    private final UserRepository userRepository;
    private final ServiceAccountRepository serviceAccountRepository;
    private final RoleAuthorizationRepository roleAuthorizationRepository;

    public AuthorizationService(AuthorizationRepository authorizationRepository, UserRepository userRepository,
                                ServiceAccountRepository serviceAccountRepository,
                                RoleAuthorizationRepository roleAuthorizationRepository) {
        this.authorizationRepository = authorizationRepository;
        this.userRepository = userRepository;
        this.serviceAccountRepository = serviceAccountRepository;
        this.roleAuthorizationRepository = roleAuthorizationRepository;
    }

    public ResponseEntity<?> createAuthorization(AuthorizationRequest authorizationRequest) {
        LOGGER.info("Attempting to create authorization");

        try {
            if(authorizationRequest.getName() == null || authorizationRequest.getName().isBlank())
                throw new RuntimeException("Authorization name is required");

            if(authorizationRepository.findByName(authorizationRequest.getName()).isPresent())
                throw new RuntimeException("Authorization already exists");

            Authorization authorization = authorizationRepository.save(new Authorization(authorizationRequest.getName()));

            LOGGER.info("Authorization successfully created");
            return ResponseEntity.ok(authorization);
        } catch(Exception ex) {
            LOGGER.error("Failed to create authorization: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
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

    public ResponseEntity<?> deleteAuthorization(String name) {
        LOGGER.info("Attempting to delete authorization");

        try {
            Authorization authorization = authorizationRepository.findByName(name)
                    .orElseThrow(() -> new RuntimeException("Authorization was not found"));

            if(userRepository.existsByAuthorizationsContaining(authorization)
                    || serviceAccountRepository.existsByAuthorizationsContaining(authorization)
                    || roleAuthorizationRepository.existsByAuthorization(authorization))
                throw new RuntimeException("Authorization is currently in use");

            authorizationRepository.delete(authorization);

            LOGGER.info("Authorization successfully deleted");
            return ResponseEntity.ok().build();
        } catch(Exception ex) {
            LOGGER.error("Failed to delete authorization: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
