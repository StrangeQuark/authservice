package com.strangequark.authservice.config;

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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A service for performing operations on JWT tokens. Uses @Service to specify that this is a Spring bean
 */
@Service
public class JwtService {

    /**
     * The JWT secret key defined in the application.properties
     */
    @Value("${jwtSecretKey}")
    private String JWT_SECRET_KEY;

    /**
     * The amount of time in milliseconds that the JWT token will expire
     */
    private final int JWT_TOKEN_EXPIRATION_TIME = 1000 * 60;//1 minute

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
     * Extract a single claim from all claims
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
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    /**
     * Generate a JWT token without extra claims, expiring after {@link #JWT_TOKEN_EXPIRATION_TIME}
     * @param userDetails The user's details, used to extract the username of the current user
     * @return Generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generate a JWT token from the extra claims and user's username, expiring after {@link #JWT_TOKEN_EXPIRATION_TIME}
     * @param extraClaims The extra claims to be added into the JWT token
     * @param userDetails The user's details, used to extract the username of the current user
     * @return Generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Retrieve and decode the JWT signing key defined in application.properties
     * @return The decoded SHA key
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET_KEY);

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
