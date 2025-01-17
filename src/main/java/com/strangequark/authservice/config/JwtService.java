package com.strangequark.authservice.config;

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
import java.util.UUID;
import java.util.function.Function;

/**
 * A service for performing operations on JWT tokens.
 */
@Service
public class JwtService {

    /**
     * The JWT access secret key defined in the application.properties
     */
    @Value("${accessSecretKey}")
    private String ACCESS_SECRET_KEY;

    /**
     * The JWT refresh secret key defined in the application.properties
     */
    @Value("${refreshSecretKey}")
    private String REFRESH_SECRET_KEY;

    /**
     * The amount of time in milliseconds that the JWT token will expire
     */
    private final int ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 10;//10 minutes
    private final int REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 14;//14 days

    /**
     * Extract the username from the JWT token
     * @param jwtToken The JWT token from which the username is to be extracted
     * @return The username contained in the JWT token
     */
    public String extractUsername(String jwtToken) {
        return extractClaim(jwtToken, Claims::getSubject);
    }

//    /**
//     * Extract the email from the JWT token
//     * @param jwtToken The JWT token from which the email is to be extracted
//     * @return The email contained in the JWT token
//     */
//    public String extractEmail(String jwtToken) {
//        return extractClaim(jwtToken, Claims::getSubject);
//    }

    /**
     * Extract a single claim from the JWT token
     * @param jwtToken The JWT token from which the claim is to be extracted
     * @param claimsResolver The function to specify which claim to extract
     * @return The specified claim to be extracted
     */
    public <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwtToken);

        return claimsResolver.apply(claims);
    }

    /**
     * Extract all the claims from the JWT token
     * @param jwtToken The JWT token from which the claims are to be extracted
     * @return The Claims contained in the JWT token
     */
    private Claims extractAllClaims(String jwtToken) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey(false))
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    /**
     * Generate a JWT token without extra claims, expiring after {@link #ACCESS_TOKEN_EXPIRATION_TIME} or {@link #REFRESH_TOKEN_EXPIRATION_TIME}
     * @param user The user object to extract the username and authorizations
     * @param refreshToken Flag to specify refresh or access token
     * @return Generated JWT token
     */
    public String generateToken(User user, boolean refreshToken) {
        return Jwts
                .builder()
                .setClaims(null)
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +
                        (refreshToken ? REFRESH_TOKEN_EXPIRATION_TIME : ACCESS_TOKEN_EXPIRATION_TIME)
                ))
                .setAudience(refreshToken ? null : user.getAuthorizations().toString())
                .signWith(getSigningKey(refreshToken), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Retrieve and decode the JWT signing key defined in application.properties
     * @return The decoded SHA key
     */
    private Key getSigningKey(boolean refreshToken) {
        String key = refreshToken ? REFRESH_SECRET_KEY : ACCESS_SECRET_KEY;
        byte[] keyBytes = Decoders.BASE64.decode(key);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Check if the JWT token is valid, belongs to the current user, and is not expired
     * @param jwtToken The JWT token to check
     * @param userDetails Used to validate if the JWT token belongs to the user
     * @return True: Token is valid, False: Token is invalid
     */
    public boolean isTokenValid(String jwtToken, UserDetails userDetails) {
        final String username = extractUsername(jwtToken);

        return (username.equals(userDetails.getUsername())) && !isTokenExpired(jwtToken);
    }

    /**
     * Check if the JWT token is expired
     * @param jwtToken The JWT to check
     * @return True: Token is expired, False: Token is not expired
     */
    private boolean isTokenExpired(String jwtToken) {
        return extractExpiration(jwtToken).before(new Date());
    }

    /**
     * Extract the expiration date from the JWT token
     * @param jwtToken The JWT to check
     * @return The expiration date of the JWT toekn
     */
    private Date extractExpiration(String jwtToken) {
        return extractClaim(jwtToken, Claims::getExpiration);
    }
}
