package com.strangequark.authservice.invitation;

import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.utility.EmailUtility; // Integration line: Email
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class InvitationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvitationService.class);
    private final InvitationRepository invitationRepository;

    @Value("${invite.only}")
    private boolean INVITE_ONLY;
    // Integration function start: Email
    @Autowired
    private EmailUtility emailUtility;
    // Integration function end: Email

    public InvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    public ResponseEntity<?> createInvitation(InvitationRequest invitationRequest) {
        LOGGER.info("Attempting to create invitation");

        try {
            if(invitationRequest.getEmail() == null || invitationRequest.getEmail().isEmpty())
                throw new RuntimeException("Email cannot be empty or null");

            String token = UUID.randomUUID().toString();
            Invitation invitation = new Invitation(invitationRequest.getEmail(), hashToken(token), LocalDateTime.now().plusDays(1));
            invitationRepository.save(invitation);
            // Integration function start: Email
            ResponseEntity<?> response = emailUtility.sendInviteEmail(invitationRequest.getEmail(), token);
            if(response.getStatusCode().value() != 200)
                LOGGER.warn("Unable to send invitation email: " + response.getBody());
            // Integration function end: Email

            LOGGER.info("Invitation successfully created");
            return ResponseEntity.ok(new InvitationResponse(invitation.getId(), token, invitation.getExpiresAt()));
        } catch(Exception ex) {
            LOGGER.error("Failed to create invitation: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }

    public ResponseEntity<?> deleteInvitation(UUID id) {
        LOGGER.info("Attempting to delete invitation");

        try {
            Invitation invitation = invitationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invitation was not found"));

            invitationRepository.delete(invitation);
            LOGGER.info("Invitation successfully deleted");
            return ResponseEntity.ok(new InvitationResponse(invitation.getId(), null, invitation.getExpiresAt()));
        } catch(Exception ex) {
            LOGGER.error("Failed to delete invitation: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }

    public ResponseEntity<?> getInviteOnly() {
        return ResponseEntity.ok(new InvitationStatusResponse(INVITE_ONLY));
    }

    public Invitation getValidInvitation(String email, String token) {
        if(token == null || token.isEmpty())
            throw new RuntimeException("Invitation is invalid or expired");

        Invitation invitation = invitationRepository.findByTokenHashForUpdate(hashToken(token))
                .orElseThrow(() -> new RuntimeException("Invitation is invalid or expired"));

        if(invitation.isUsed() || invitation.getExpiresAt().isBefore(LocalDateTime.now()) || !invitation.getEmail().equals(email))
            throw new RuntimeException("Invitation is invalid or expired");

        return invitation;
    }

    public void useInvitation(Invitation invitation) {
        invitation.setUsed(true);
        invitationRepository.save(invitation);
    }

    @Scheduled(fixedRateString = "${invitation.cleanup.interval}")
    public void deleteExpiredInvitations() {
        invitationRepository.deleteByUsedTrueOrExpiresAtBefore(LocalDateTime.now());
    }

    private String hashToken(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch(Exception ex) {
            throw new RuntimeException("Unable to hash invitation token");
        }
    }
}
