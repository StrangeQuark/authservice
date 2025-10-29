package com.strangequark.authservice.access;

import com.strangequark.authservice.auth.AuthenticationResponse;
import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import com.strangequark.authservice.utility.TelemetryUtility; // Integration line: Telemetry
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Integration line: Telemetry
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

    /** Integration function start: Telemetry
     * {@link TelemetryUtility} for sending telemetry events to the Kafka
     */
    @Autowired
    TelemetryUtility telemetryUtility;
    // Integration function end: Telemetry
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
        LOGGER.info("Attempting to serve access token");

        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken, true))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Verify the refresh token against the User's refresh token
            if(user.getRefreshToken() == null || !user.getRefreshToken().equals(authToken))
                        throw new RuntimeException("Refresh token is invalid");

            //Create a JWT token to authenticate the user
            String accessToken = jwtService.generateToken(user, false);
            // Send a telemetry event for user access token - Integration line: Telemetry
            telemetryUtility.sendTelemetryEvent("user-authenticate", user.getId(), null); // Integration line: Telemetry

            //Return a 200 response with the jwtToken
            LOGGER.info("Access token successfully served");
            return ResponseEntity.ok(new AuthenticationResponse(accessToken));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
