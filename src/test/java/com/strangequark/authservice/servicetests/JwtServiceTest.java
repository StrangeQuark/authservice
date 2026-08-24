package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.authorization.Authorization;
import com.strangequark.authservice.authorization.RoleAuthorization;
import com.strangequark.authservice.user.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JwtServiceTest extends BaseServiceTest {
    @Test
    void userAuthorizationsAreAddedToAccessTokenTest() {
        Authorization authorization = authorizationRepository.save(new Authorization("TEST_AUTHORIZATION"));
        testUser.getAuthorizations().add(authorization);

        String token = jwtService.generateToken(testUser, false);
        List<String> authorizations = jwtService.extractClaim(token,
                claims -> claims.get("authorizations", List.class), false);

        Assertions.assertTrue(authorizations.contains("TEST_AUTHORIZATION"));
    }
    // Integration function start: Email
    @Test
    void superRoleAuthorizationsAreAddedToAccessTokenTest() {
        Authorization authorization = authorizationRepository.findByName("EMAIL_API_ACCESS").orElseThrow();
        roleAuthorizationRepository.save(new RoleAuthorization(Role.SUPER, authorization));
        setupSuperUser();

        String token = jwtService.generateToken(testSuper, false);
        List<String> authorizations = jwtService.extractClaim(token,
                claims -> claims.get("authorizations", List.class), false);

        Assertions.assertTrue(authorizations.contains("EMAIL_API_ACCESS"));
    }// Integration function end: Email
}
