package com.strangequark.authservice.authorization;

import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.user.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RoleAuthorizationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleAuthorizationService.class);

    private final AuthorizationRepository authorizationRepository;
    private final RoleAuthorizationRepository roleAuthorizationRepository;

    public RoleAuthorizationService(AuthorizationRepository authorizationRepository,
                                    RoleAuthorizationRepository roleAuthorizationRepository) {
        this.authorizationRepository = authorizationRepository;
        this.roleAuthorizationRepository = roleAuthorizationRepository;
    }

    public ResponseEntity<?> addRoleAuthorization(RoleAuthorizationRequest roleAuthorizationRequest) {
        LOGGER.info("Attempting to add role authorization");

        try {
            if(roleAuthorizationRequest.getRole() == null)
                throw new RuntimeException("Role is required");

            Authorization authorization = authorizationRepository.findByName(roleAuthorizationRequest.getAuthorization())
                    .orElseThrow(() -> new RuntimeException("Authorization was not found"));

            if(roleAuthorizationRepository.findByRoleAndAuthorization(roleAuthorizationRequest.getRole(), authorization).isPresent())
                throw new RuntimeException("Role authorization already exists");

            RoleAuthorization roleAuthorization = roleAuthorizationRepository.save(
                    new RoleAuthorization(roleAuthorizationRequest.getRole(), authorization)
            );

            LOGGER.info("Role authorization successfully added");
            return ResponseEntity.ok(roleAuthorization);
        } catch(Exception ex) {
            LOGGER.error("Failed to add role authorization: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }

    public ResponseEntity<?> getRoleAuthorizations(Role role) {
        LOGGER.info("Attempting to get role authorizations");

        try {
            return ResponseEntity.ok(roleAuthorizationRepository.findByRole(role));
        } catch(Exception ex) {
            LOGGER.error("Failed to get role authorizations: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }

    public ResponseEntity<?> removeRoleAuthorization(RoleAuthorizationRequest roleAuthorizationRequest) {
        LOGGER.info("Attempting to remove role authorization");

        try {
            if(roleAuthorizationRequest.getRole() == null)
                throw new RuntimeException("Role is required");

            Authorization authorization = authorizationRepository.findByName(roleAuthorizationRequest.getAuthorization())
                    .orElseThrow(() -> new RuntimeException("Authorization was not found"));

            RoleAuthorization roleAuthorization = roleAuthorizationRepository
                    .findByRoleAndAuthorization(roleAuthorizationRequest.getRole(), authorization)
                    .orElseThrow(() -> new RuntimeException("Role authorization was not found"));

            roleAuthorizationRepository.delete(roleAuthorization);

            LOGGER.info("Role authorization successfully removed");
            return ResponseEntity.ok().build();
        } catch(Exception ex) {
            LOGGER.error("Failed to remove role authorization: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
