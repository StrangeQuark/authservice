package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.auth.AuthenticationService;
import com.strangequark.authservice.auth.RegistrationRequest;
import com.strangequark.authservice.invitation.Invitation;
import com.strangequark.authservice.invitation.InvitationRequest;
import com.strangequark.authservice.invitation.InvitationResponse;
import com.strangequark.authservice.invitation.InvitationService;
import com.strangequark.authservice.utility.EmailUtility;
import com.strangequark.authservice.utility.EmailType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class InvitationServiceTest extends BaseServiceTest {
    @Autowired
    private InvitationService invitationService;
    @Autowired
    private AuthenticationService authenticationService;
    @MockitoBean
    private EmailUtility emailUtility;

    @BeforeEach
    void setupInvitationService() {
        when(emailUtility.sendInviteEmail(anyString(), anyString())).thenReturn(ResponseEntity.ok().build());
        when(emailUtility.sendEmail(anyString(), eq(EmailType.REGISTER))).thenReturn(ResponseEntity.ok().build());
        ReflectionTestUtils.setField(authenticationService, "INVITE_ONLY", true);
    }

    @Test
    void validInvitationRegistersUserTest() {
        InvitationResponse invitationResponse = createInvitation("invite@test.com");
        RegistrationRequest registrationRequest = new RegistrationRequest("inviteUser", "invite@test.com", "password");
        registrationRequest.setInviteToken(invitationResponse.getToken());

        ResponseEntity<?> response = authenticationService.register(registrationRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(invitationRepository.findById(invitationResponse.getId()).get().isUsed());
    }

    @Test
    void missingInvitationBlocksRegistrationTest() {
        RegistrationRequest registrationRequest = new RegistrationRequest("inviteUser", "invite@test.com", "password");

        ResponseEntity<?> response = authenticationService.register(registrationRequest);

        Assertions.assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void expiredInvitationBlocksRegistrationTest() {
        InvitationResponse invitationResponse = createInvitation("invite@test.com");
        Invitation invitation = invitationRepository.findById(invitationResponse.getId()).get();
        invitation.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        invitationRepository.save(invitation);

        RegistrationRequest registrationRequest = new RegistrationRequest("inviteUser", "invite@test.com", "password");
        registrationRequest.setInviteToken(invitationResponse.getToken());

        ResponseEntity<?> response = authenticationService.register(registrationRequest);

        Assertions.assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void usedInvitationBlocksRegistrationTest() {
        InvitationResponse invitationResponse = createInvitation("invite@test.com");
        RegistrationRequest registrationRequest = new RegistrationRequest("inviteUser", "invite@test.com", "password");
        registrationRequest.setInviteToken(invitationResponse.getToken());
        authenticationService.register(registrationRequest);

        registrationRequest.setUsername("secondInviteUser");
        ResponseEntity<?> response = authenticationService.register(registrationRequest);

        Assertions.assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void mismatchedInvitationBlocksRegistrationTest() {
        InvitationResponse invitationResponse = createInvitation("invite@test.com");
        RegistrationRequest registrationRequest = new RegistrationRequest("inviteUser", "different@test.com", "password");
        registrationRequest.setInviteToken(invitationResponse.getToken());

        ResponseEntity<?> response = authenticationService.register(registrationRequest);

        Assertions.assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void deleteInvitationTest() {
        InvitationResponse invitationResponse = createInvitation("invite@test.com");

        ResponseEntity<?> response = invitationService.deleteInvitation(invitationResponse.getId());

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(invitationRepository.findById(invitationResponse.getId()).isEmpty());
    }

    @Test
    void deleteAllInvitationsTest() {
        createInvitation("first@test.com");
        createInvitation("second@test.com");

        ResponseEntity<?> response = invitationService.deleteAllInvitations();

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(invitationRepository.findAll().isEmpty());
    }

    @Test
    void getAllInvitationsTest() {
        createInvitation("invite@test.com");

        ResponseEntity<?> response = invitationService.getAllInvitations();

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals(1, ((java.util.List<?>) response.getBody()).size());
    }

    private InvitationResponse createInvitation(String email) {
        InvitationRequest invitationRequest = new InvitationRequest();
        invitationRequest.setEmail(email);

        ResponseEntity<?> response = invitationService.createInvitation(invitationRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        return (InvitationResponse) response.getBody();
    }
}
