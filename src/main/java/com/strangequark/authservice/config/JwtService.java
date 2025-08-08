package com.strangequark.authservice.config;

import com.strangequark.authservice.serviceaccount.ServiceAccount;
import com.strangequark.authservice.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * A service for performing operations on JWT tokens.
 */
@Service
public class JwtService {

    /**
     * The JWT access secret key defined in the application.properties
     */
    @Value("${ACCESS_SECRET_KEY}")
    private String ACCESS_SECRET_KEY;

    /**
     * The JWT refresh secret key defined in the application.properties
     */
    @Value("${REFRESH_SECRET_KEY}")
    private String REFRESH_SECRET_KEY;

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
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey(isRefreshToken))
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    /**
     * Generate a JWT token without extra claims, expiring after {@link #ACCESS_TOKEN_EXPIRATION_TIME} or {@link #REFRESH_TOKEN_EXPIRATION_TIME}
     * @param user The user object to extract the username and authorizations
     * @param isRefreshToken Flag to specify refresh or access token
     * @return Generated JWT token
     */
    public String generateToken(User user, boolean isRefreshToken) {
        return Jwts
                .builder()
                .setClaims(null)
                .setId(user.getId().toString())
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +
                        (isRefreshToken ? REFRESH_TOKEN_EXPIRATION_TIME : ACCESS_TOKEN_EXPIRATION_TIME)
                ))
                .setAudience(isRefreshToken ? null : user.getAuthorizations().toString())
                .signWith(getSigningKey(isRefreshToken), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate a JWT token for service accounts without extra claims, expiring after {@link #ACCESS_TOKEN_EXPIRATION_TIME} or {@link #REFRESH_TOKEN_EXPIRATION_TIME}
     * @param serviceAccount The service account object to extract the clientId
     * @param isRefreshToken Flag to specify refresh or access token
     * @return Generated JWT token
     */
    public String generateServiceAccountToken(ServiceAccount serviceAccount, boolean isRefreshToken) {
        return Jwts
                .builder()
                .setClaims(null)
                .setId(serviceAccount.getId().toString())
                .setSubject(serviceAccount.getClientId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +
                        (isRefreshToken ? REFRESH_TOKEN_EXPIRATION_TIME : ACCESS_TOKEN_EXPIRATION_TIME)
                ))
//                .setAudience(serviceAccount.getAuthorizations().toString())
                .signWith(getSigningKey(isRefreshToken), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Retrieve and decode the JWT signing key defined in application.properties
     * @param isRefreshToken Flag to specify refresh or access token
     * @return The decoded SHA key
     */
    private Key getSigningKey(boolean isRefreshToken) {
        String key = isRefreshToken ? REFRESH_SECRET_KEY : ACCESS_SECRET_KEY;
        byte[] keyBytes = Decoders.BASE64.decode(key);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Check if the JWT token is valid, belongs to the current user, and is not expired
     * @param jwtToken The JWT token to check
     * @param userDetails Used to validate if the JWT token belongs to the user
     * @param isRefreshToken Flag to specify refresh or access token
     * @return True: Token is valid, False: Token is invalid
     */
    public boolean isTokenValid(String jwtToken, UserDetails userDetails,boolean isRefreshToken) {
        final String username = extractUsername(jwtToken, isRefreshToken);

        return (username.equals(userDetails.getUsername())) && !isTokenExpired(jwtToken, isRefreshToken);
    }

    /**
     * Check if the JWT token is expired
     * @param jwtToken The JWT to check
     * @param isRefreshToken Flag to specify refresh or access token
     * @return True: Token is expired, False: Token is not expired
     */
    private boolean isTokenExpired(String jwtToken, boolean isRefreshToken) {
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
}
