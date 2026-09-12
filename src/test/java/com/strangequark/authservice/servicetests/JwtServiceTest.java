package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.authorization.Authorization;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
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
        Assertions.assertFalse(authorizations.contains("VAULT_API_ACCESS"));
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
        Assertions.assertEquals("email", claims.get("clientId", String.class));
    }

    @Test
    void malformedAccessTokenIsRejectedTest() {
        Assertions.assertThrows(Exception.class, () -> jwtService.extractUsername("invalid token", false));
    }

    @Test
    void refreshTokenIsRejectedAsAccessTokenTest() {
        String token = jwtService.generateToken(testUser, true);

        Assertions.assertThrows(Exception.class, () -> jwtService.extractUsername(token, false));
    }

    @Test
    void accessTokenWithWrongIssuerIsRejectedTest() {
        String token = createAccessToken("wrong-issuer", new Date(System.currentTimeMillis() + 60000),
                "token-id", testUser.getId().toString());

        Assertions.assertThrows(Exception.class, () -> jwtService.extractUsername(token, false));
    }

    @Test
    void expiredAccessTokenIsRejectedTest() {
        String token = createAccessToken("msinit-authservice", new Date(System.currentTimeMillis() - 60000),
                "token-id", testUser.getId().toString());

        Assertions.assertThrows(Exception.class, () -> jwtService.extractUsername(token, false));
    }

    @Test
    void accessTokenWithoutJtiIsRejectedTest() {
        String token = createAccessToken("msinit-authservice", new Date(System.currentTimeMillis() + 60000),
                null, testUser.getId().toString());

        Assertions.assertThrows(Exception.class, () -> jwtService.extractUsername(token, false));
    }

    @Test
    void accessTokenWithoutPrincipalIdIsRejectedTest() {
        String token = createAccessToken("msinit-authservice", new Date(System.currentTimeMillis() + 60000),
                "token-id", null);

        Assertions.assertThrows(Exception.class, () -> jwtService.extractUsername(token, false));
    }

    @Test
    void superRoleAuthorizationsAreAddedToAccessTokenTest() {
        setupSuperUser();

        String token = jwtService.generateToken(testSuper, false);
        List<String> authorizations = jwtService.extractClaim(token,
                claims -> claims.get("authorizations", List.class), false);

        Assertions.assertTrue(authorizations.contains("EMAIL_API_ACCESS"));
    }

    private String createAccessToken(String issuer, Date expiration, String jti, String principalId) {
        JwtBuilder builder = Jwts.builder()
                .setIssuer(issuer)
                .setSubject(testUser.getUsername())
                .setExpiration(expiration)
                .claim("tokenType", "ACCESS");

        if(jti != null)
            builder.setId(jti);

        if(principalId != null)
            builder.claim("principalId", principalId);

        return builder.signWith(getPrivateKey(), SignatureAlgorithm.RS256).compact();
    }

    private Key getPrivateKey() {
        try {
            String privateKey = (String) ReflectionTestUtils.getField(jwtService, "JWT_PRIVATE_KEY");
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Decoders.BASE64.decode(privateKey)));
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
