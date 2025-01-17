package com.strangequark.authservice.access;

import com.strangequark.authservice.auth.AuthenticationResponse;
import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * {@link Service} for serving access tokens
 */
@Service
public class AccessService {

    /**
     * {@link UserRepository} for fetching {@link User} from the database
     */
    private final UserRepository userRepository;

    /**
     * {@link JwtService} for generating a JWT token to return with the registration response
     */
    private final JwtService jwtService;

    public AccessService(UserRepository userRepository, JwtService jwtService) {
        this. userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Business logic checking refresh token, and returning an access token if successful
     * @return {@link ResponseEntity} with a {@link AuthenticationResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> serveAccessToken() {
        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Verify the refresh token against the User's refresh token
            if(user.getRefreshToken() == null || !user.getRefreshToken().equals(authToken))
                return ResponseEntity.status(401).body(
                        new ErrorResponse("Refresh token is invalid")
                );

            //Create a JWT token to authenticate the user
            String accessToken = jwtService.generateToken(user, false);

            //Return a 200 response with the jwtToken
            return ResponseEntity.ok(new AuthenticationResponse(accessToken));

        } catch (AuthenticationException authenticationException) {
            //Throw a 401 (Unauthorized) error if invalid credentials are given
            return ResponseEntity.status(401).body(
                    new ErrorResponse("Invalid credentials")
            );
        }
    }
}
