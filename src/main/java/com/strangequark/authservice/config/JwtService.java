package com.strangequark.authservice.config;

import com.strangequark.authservice.authorization.Authorization;
import com.strangequark.authservice.authorization.RoleAuthorization;
import com.strangequark.authservice.authorization.RoleAuthorizationRepository;
import com.strangequark.authservice.serviceaccount.ServiceAccount;
import com.strangequark.authservice.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * A service for performing operations on JWT tokens.
 */
@Service
public class JwtService {
    private final RoleAuthorizationRepository roleAuthorizationRepository;

    public JwtService(RoleAuthorizationRepository roleAuthorizationRepository) {
        this.roleAuthorizationRepository = roleAuthorizationRepository;
    }

    /**
     * The JWT private key defined in the application.properties
     */
    @Value("${JWT_PRIVATE_KEY}")
    private String JWT_PRIVATE_KEY;

    /**
     * The JWT public key defined in the application.properties
     */
    @Value("${JWT_PUBLIC_KEY}")
    private String JWT_PUBLIC_KEY;

    @Value("${JWT_ISSUER}")
    private String JWT_ISSUER;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    /**
     * The amount of time in milliseconds that an access token will expire
     */
    private final int ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 10;//10 minutes

    /**
     * The amount of time in milliseconds that a refresh token will expire
     */
    private final int REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 14;//14 days

    /**
     * Extract the username from the JWT token
     * @param jwtToken The JWT token from which the username is to be extracted
     * @param isRefreshToken Flag to specify refresh or access token
     * @return The username contained in the JWT token
     */
    public String extractUsername(String jwtToken, boolean isRefreshToken) {
        return extractClaim(jwtToken, Claims::getSubject, isRefreshToken);
    }

    /**
     * Extract a single claim from the JWT token
     * @param jwtToken The JWT token from which the claim is to be extracted
     * @param claimsResolver The function to specify which claim to extract
     * @param isRefreshToken Flag to specify refresh or access token
     * @return The specified claim to be extracted
     */
    public <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver, boolean isRefreshToken) {
        final Claims claims = extractAllClaims(jwtToken, isRefreshToken);

        return claimsResolver.apply(claims);
    }

    /**
     * Extract all the claims from the JWT token
     * @param jwtToken The JWT token from which the claims are to be extracted
     * @param isRefreshToken Flag to specify refresh or access token
     * @return The Claims contained in the JWT token
     */
    private Claims extractAllClaims(String jwtToken, boolean isRefreshToken) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(getPublicKey())
                .requireIssuer(JWT_ISSUER)
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();

        if(!claims.get("tokenType", String.class).equals(isRefreshToken ? "REFRESH" : "ACCESS"))
            throw new RuntimeException("JWT token type is invalid");

        if(claims.getId() == null || claims.get("principalId", String.class) == null)
            throw new RuntimeException("JWT is missing required claims");

        return claims;
    }

    /**
     * Generate a JWT token without extra claims, expiring after {@link #ACCESS_TOKEN_EXPIRATION_TIME} or {@link #REFRESH_TOKEN_EXPIRATION_TIME}
     * @param user The user object to extract the username and authorizations
     * @param isRefreshToken Flag to specify refresh or access token
     * @return Generated JWT token
     */
    public String generateToken(User user, boolean isRefreshToken) {
        Set<String> authorizations = getAuthorizations(user);

        return Jwts
                .builder()
                .setClaims(null)
                .setId(UUID.randomUUID().toString())
                .setIssuer(JWT_ISSUER)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +
                        (isRefreshToken ? REFRESH_TOKEN_EXPIRATION_TIME : ACCESS_TOKEN_EXPIRATION_TIME)
                ))
                .claim("principalId", user.getId().toString())
                .claim("principalType", "USER")
                .claim("tokenType", isRefreshToken ? "REFRESH" : "ACCESS")
                .claim("authorizations", authorizations)
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * Generate a JWT token for service accounts without extra claims, expiring after {@link #ACCESS_TOKEN_EXPIRATION_TIME} or {@link #REFRESH_TOKEN_EXPIRATION_TIME}
     * @param serviceAccount The service account object to extract the clientId
     * @param isRefreshToken Flag to specify refresh or access token
     * @return Generated JWT token
     */
    public String generateServiceAccountToken(ServiceAccount serviceAccount, boolean isRefreshToken) {
        Set<String> authorizations = getAuthorizationNames(serviceAccount.getAuthorizations());

        return Jwts
                .builder()
                .setClaims(null)
                .setId(UUID.randomUUID().toString())
                .setIssuer(JWT_ISSUER)
                .setSubject(serviceAccount.getClientId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +
                        (isRefreshToken ? REFRESH_TOKEN_EXPIRATION_TIME : ACCESS_TOKEN_EXPIRATION_TIME)
                ))
                .claim("principalId", serviceAccount.getId().toString())
                .claim("principalType", "SERVICE_ACCOUNT")
                .claim("tokenType", isRefreshToken ? "REFRESH" : "ACCESS")
                .claim("authorizations", authorizations)
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    private Set<String> getAuthorizations(User user) {
        Set<String> authorizations = getAuthorizationNames(user.getAuthorizations());

        for(RoleAuthorization roleAuthorization : roleAuthorizationRepository.findByRole(user.getRole()))
            authorizations.add(roleAuthorization.getAuthorization().getName());

        return authorizations;
    }

    private Set<String> getAuthorizationNames(Set<Authorization> authorizationEntities) {
        Set<String> authorizations = new HashSet<>();

        for(Authorization authorization : authorizationEntities)
            authorizations.add(authorization.getName());

        return authorizations;
    }

    /**
     * Retrieve and decode the JWT private key defined in application.properties
     * @return The decoded RSA key
     */
    private Key getPrivateKey() {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Decoders.BASE64.decode(JWT_PRIVATE_KEY)));
        } catch(Exception ex) {
            throw new RuntimeException("Failed to decode JWT private key", ex);
        }
    }

    private Key getPublicKey() {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Decoders.BASE64.decode(JWT_PUBLIC_KEY)));
        } catch(Exception ex) {
            throw new RuntimeException("Failed to decode JWT public key", ex);
        }
    }

    /**
     * Check if the JWT token is valid, belongs to the current user, and is not expired
     * @param jwtToken The JWT token to check
     * @param userDetails Used to validate if the JWT token belongs to the user
     * @param isRefreshToken Flag to specify refresh or access token
     * @return True: Token is valid, False: Token is invalid
     */
    public boolean isTokenValid(String jwtToken, UserDetails userDetails, boolean isRefreshToken) {
        final String username = extractUsername(jwtToken, isRefreshToken);

        return (username.equals(userDetails.getUsername())) && !isTokenExpired(jwtToken, isRefreshToken);
    }

    /**
     * Check if the JWT token is expired
     * @param jwtToken The JWT to check
     * @param isRefreshToken Flag to specify refresh or access token
     * @return True: Token is expired, False: Token is not expired
     */
    public boolean isTokenExpired(String jwtToken, boolean isRefreshToken) {
        return extractExpiration(jwtToken, isRefreshToken).before(new Date());
    }

    /**
     * Extract the expiration date from the JWT token
     * @param jwtToken The JWT to check
     * @param isRefreshToken Flag to specify refresh or access token
     * @return The expiration date of the JWT toekn
     */
    private Date extractExpiration(String jwtToken, boolean isRefreshToken) {
        return extractClaim(jwtToken, Claims::getExpiration, isRefreshToken);
    }

    public ResponseCookie buildTokenCookie(String tokenName, String token, boolean isRefreshToken) {
        return ResponseCookie.from(tokenName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .maxAge(isRefreshToken ? REFRESH_TOKEN_EXPIRATION_TIME / 1000 : ACCESS_TOKEN_EXPIRATION_TIME / 1000)
                .path("/")
                .build();
    }

    public ResponseCookie clearTokenCookie(String tokenName) {
        return ResponseCookie.from(tokenName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .maxAge(0)
                .path("/")
                .build();
    }
}
