package com.strangequark.authservice.access;

import com.strangequark.authservice.auth.AuthenticationResponse;
import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
     * {@link Logger} for writing {@link AccessService} application logs
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessService.class);

    /**
     * {@link UserRepository} for fetching {@link User} from the database
     */
    private final UserRepository userRepository;

    /**
     * {@link JwtService} for generating a JWT token to return with the registration response
     */
    private final JwtService jwtService;

    /**
     * Constructs a new {@code AccessService} with the given dependencies.
     *
     * @param userRepository {@link UserRepository} for fetching {@link User} from the database
     * @param jwtService     {@link JwtService} for generating a JWT access token
     */
    public AccessService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
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

            LOGGER.info("Attempt to serve accessToken for jwt: " + authToken);

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken, true))
                    .orElseThrow(() -> {
                        LOGGER.error("User was not found for jwt: " + authToken);
                        return new UsernameNotFoundException("User not found");
                    });

            //Verify the refresh token against the User's refresh token
            if(user.getRefreshToken() == null || !user.getRefreshToken().equals(authToken)) {
                LOGGER.error("The refresh token is invalid: " + authToken);

                return ResponseEntity.status(401).body(
                        new ErrorResponse("Refresh token is invalid")
                );
            }

            //Create a JWT token to authenticate the user
            String accessToken = jwtService.generateToken(user, false);

            //Return a 200 response with the jwtToken
            LOGGER.info("Access token successfully served");
            return ResponseEntity.ok(new AuthenticationResponse(accessToken));

        } catch (NullPointerException exception) {
            LOGGER.error(exception.getMessage());
            return ResponseEntity.status(500).body(
                    new ErrorResponse("NPE - trouble getting the request")
            );
        }
    }
}
