package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.authorization.Authorization;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    @Test
    void userRoleAuthorizationsAreAddedToAccessTokenTest() {
        String token = jwtService.generateToken(testUser, false);
        List<String> authorizations = jwtService.extractClaim(token,
                claims -> claims.get("authorizations", List.class), false);

        Assertions.assertTrue(authorizations.contains("AUTH_API_ACCESS"));
        Assertions.assertTrue(authorizations.contains("FILE_API_ACCESS"));
        Assertions.assertTrue(authorizations.contains("VAULT_API_ACCESS"));
        Assertions.assertFalse(authorizations.contains("EMAIL_API_ACCESS"));
        Assertions.assertFalse(authorizations.contains("TELEMETRY_API_ACCESS"));
    }

    @Test
    void tokenClaimsAreAddedToAccessTokenTest() {
        String token = jwtService.generateToken(testUser, false);
        Claims claims = jwtService.extractClaim(token, jwtClaims -> jwtClaims, false);
        String header = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);

        Assertions.assertEquals("msinit-authservice", claims.getIssuer());
        Assertions.assertEquals("ACCESS", claims.get("tokenType", String.class));
        Assertions.assertEquals(testUser.getId().toString(), claims.get("principalId", String.class));
        Assertions.assertEquals("USER", claims.get("principalType", String.class));
        Assertions.assertNotNull(claims.getId());
        Assertions.assertNotEquals(testUser.getId().toString(), claims.getId());
        Assertions.assertTrue(header.contains("RS256"));
    }

    @Test
    void serviceAccountClaimsAreAddedToAccessTokenTest() {
        String token = jwtService.generateServiceAccountToken(
                serviceAccountRepository.findByClientId("email").get(), false
        );
        Claims claims = jwtService.extractClaim(token, jwtClaims -> jwtClaims, false);

        Assertions.assertEquals("SERVICE_ACCOUNT", claims.get("principalType", String.class));
    }
    // Integration function start: Email
    @Test
    void superRoleAuthorizationsAreAddedToAccessTokenTest() {
        setupSuperUser();

        String token = jwtService.generateToken(testSuper, false);
        List<String> authorizations = jwtService.extractClaim(token,
                claims -> claims.get("authorizations", List.class), false);

        Assertions.assertTrue(authorizations.contains("EMAIL_API_ACCESS"));
    }// Integration function end: Email
}
