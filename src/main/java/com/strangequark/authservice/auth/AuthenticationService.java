package com.strangequark.authservice.auth;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import com.strangequark.authservice.utility.EmailType; // Integration line: Email
import com.strangequark.authservice.utility.EmailUtility; // Integration line: Email
import com.strangequark.authservice.utility.TelemetryUtility; // Integration line: Telemetry
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException; // Integration line: Email

import java.util.LinkedHashSet;
import java.util.Map; // Integration line: Telemetry

/**
 * {@link Service} for registering and authenticating user requests
 */
@Service
public class AuthenticationService {
    /**
     * {@link Logger} for writing {@link AuthenticationService} application logs
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * {@link UserRepository} for fetching {@link com.strangequark.authservice.user.User} from the database
     */
    private final UserRepository userRepository;

    /**
     * {@link PasswordEncoder} for encoding our password when registering a new user to the database
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * {@link JwtService} for generating a JWT token to return with the registration response
     */
    private final JwtService jwtService;

    /**
     * {@link AuthenticationManager} for authenticating JWT tokens
     */
    private final AuthenticationManager authenticationManager;

    /** Integration function start: Email
     * {@link EmailUtility} for sending requests to email service
     */
    @Autowired
    EmailUtility emailUtility;
    // Integration function end: Email
    /** Integration function start: Telemetry
     * {@link TelemetryUtility} for sending telemetry events to the Kafka
     */
    @Autowired
    TelemetryUtility telemetryUtility;
    // Integration function end: Telemetry
    /**
     * Constructs a new {@code AuthenticationService} with the given dependencies.
     *
     * @param userRepository {@link UserRepository} for performing transactions on the User database
     * @param passwordEncoder {@link PasswordEncoder} for encoding/decoding passwords in the User database
     * @param jwtService {@link JwtService} for generating JWT tokens
     * @param authenticationManager {@link AuthenticationManager} for authenticating JWT tokens
     */
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Business logic for registering a new user
     * @param registrationRequest Request body containing registration details
     * @return {@link ResponseEntity} with a {@link RegistrationResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> register(RegistrationRequest registrationRequest) {
        LOGGER.info("Attempting to register user");
        String responseMessage = "";

        try {
            //Check if the username has already been registered
            if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent())
                throw new RuntimeException("Username already registered");

            //Check if the email has already been registered
            if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent())
                throw new RuntimeException("Email already registered");

            LOGGER.debug("Attempting to build user object");

            //Build the user object to be saved to the database
            User user = new User(registrationRequest.getUsername(), registrationRequest.getEmail(), Role.USER,
                    false, new LinkedHashSet<>(), passwordEncoder.encode(registrationRequest.getPassword()));

            //Send an email so the user can enable their account   -   Integration function start: Email
            LOGGER.debug("Attempting to send registration email");
            try {
                ResponseEntity<?> response = emailUtility.sendEmail(registrationRequest.getEmail(), EmailType.REGISTER);

                if (response.getStatusCode().value() != 200) {
                    LOGGER.warn("Error when calling email service: " + response.getBody());
                    LOGGER.debug("Continuing user registration, setting user to enabled");
                    user.setEnabled(true);
                    responseMessage = "Registered without email";
                }
            } catch (ResourceAccessException resourceAccessException) {
                //If we are unable to reach the email service, proceed with user creation and set user as enabled
                LOGGER.warn("Unable to reach email service: " + resourceAccessException.getMessage());
                LOGGER.debug("Continuing to register user, setting user to enabled");// Integration function end: Email
                user.setEnabled(true);
                responseMessage = "Registered without email";
            }// Integration line: Email

            //Save the user to the database
            LOGGER.debug("Saving user to database");
            userRepository.save(user);
            // Send a telemetry event for user registration - Integration line: Telemetry
            telemetryUtility.sendTelemetryEvent("user-register", Map.of("userId", user.getId())); // Integration line: Telemetry

            //Return a 200 response with a JWT token
            LOGGER.info("User successfully created");
            return ResponseEntity.ok(new RegistrationResponse(responseMessage));
        } catch (Exception ex) {
            LOGGER.error("Failed to register user: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }

    /**
     * Business logic for authenticating a user
     * @param authenticationRequest
     * @return {@link ResponseEntity} with a {@link AuthenticationResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> authenticate(AuthenticationRequest authenticationRequest) {
        LOGGER.info("Attempting to authenticate request");

        try {
            //Authenticate the user, throw an AuthenticationException if the username and password combination are incorrect
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(authenticationRequest.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            LOGGER.debug("User found, creating refresh token");

            //Create a JWT token to authenticate the user
            String refreshToken = jwtService.generateToken(user, true);

            //Add the refresh token to the user and save
            LOGGER.debug("Saving refresh token to user in database");
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
            // Send a telemetry event for user authentication - Integration line: Telemetry
            telemetryUtility.sendTelemetryEvent("user-authenticate", Map.of("userId", user.getId())); // Integration line: Telemetry

            //Return a 200 response with the JWT refresh token
            LOGGER.info("Authentication successful");
            return ResponseEntity.ok(new AuthenticationResponse(refreshToken));
        } catch (AuthenticationException ex) {
            LOGGER.error("Failed to authenticate user: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(401).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
