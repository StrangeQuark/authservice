package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.authorization.Authorization;
import com.strangequark.authservice.user.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthorizationInitializerTest extends BaseServiceTest {

    @Test
    void standardAuthorizationsAreInitializedTest() {
        Authorization authAuthorization = authorizationRepository.findByName("AUTH_API_ACCESS").get();
        Authorization emailAuthorization = authorizationRepository.findByName("EMAIL_API_ACCESS").get();
        Authorization fileAuthorization = authorizationRepository.findByName("FILE_API_ACCESS").get();
        Authorization vaultAuthorization = authorizationRepository.findByName("VAULT_API_ACCESS").get();
        Authorization telemetryAuthorization = authorizationRepository.findByName("TELEMETRY_API_ACCESS").get();
        Authorization invitationManagementAuthorization = authorizationRepository.findByName("INVITATION_MANAGEMENT").get();
        Authorization userManagementAuthorization = authorizationRepository.findByName("USER_MANAGEMENT").get();
        Authorization authorizationManagementAuthorization = authorizationRepository.findByName("AUTHORIZATION_MANAGEMENT").get();
        Authorization emailTemplateManagementAuthorization = authorizationRepository.findByName("EMAIL_TEMPLATE_MANAGEMENT").get();

        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.USER, authAuthorization).isPresent());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.USER, fileAuthorization).isPresent());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.USER, vaultAuthorization).isEmpty());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.SUPER, authAuthorization).isPresent());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.SUPER, emailAuthorization).isPresent());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.SUPER, fileAuthorization).isPresent());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.SUPER, vaultAuthorization).isPresent());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.USER, telemetryAuthorization).isEmpty());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.SUPER, invitationManagementAuthorization).isPresent());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.SUPER, userManagementAuthorization).isPresent());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.SUPER, authorizationManagementAuthorization).isPresent());
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.SUPER, emailTemplateManagementAuthorization).isPresent());
    }
}
