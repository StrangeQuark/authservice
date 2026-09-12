package com.strangequark.authservice.auth;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.invitation.Invitation;
import com.strangequark.authservice.invitation.InvitationService;
import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import com.strangequark.authservice.utility.EmailType;
import com.strangequark.authservice.utility.EmailUtility;
import com.strangequark.authservice.utility.TelemetryUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashSet;
import java.util.Map;

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

    private final InvitationService invitationService;

    @Value("${invite.only}")
    private boolean INVITE_ONLY;
    @Value("${emailservice.integration}")
    private boolean emailserviceIntegration;

    /**
     * {@link EmailUtility} for sending requests to email service
     */
    @Autowired
    EmailUtility emailUtility;
    /**
     * {@link TelemetryUtility} for sending telemetry events to the Kafka
     */
    @Autowired
    TelemetryUtility telemetryUtility;
    /**
     * Constructs a new {@code AuthenticationService} with the given dependencies.
     *
     * @param userRepository {@link UserRepository} for performing transactions on the User database
     * @param passwordEncoder {@link PasswordEncoder} for encoding/decoding passwords in the User database
     * @param jwtService {@link JwtService} for generating JWT tokens
     * @param authenticationManager {@link AuthenticationManager} for authenticating JWT tokens
     */
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                                 AuthenticationManager authenticationManager, InvitationService invitationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.invitationService = invitationService;
    }

    /**
     * Business logic for registering a new user
     * @param registrationRequest Request body containing registration details
     * @return {@link ResponseEntity} with a {@link RegistrationResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    @Transactional
    public ResponseEntity<?> register(RegistrationRequest registrationRequest) {
        LOGGER.info("Attempting to register user");

        try {
            //Null checks
            if(registrationRequest.getUsername() == null || registrationRequest.getUsername().isEmpty())
                throw new RuntimeException("Username cannot be empty or null");
            if(registrationRequest.getEmail() == null || registrationRequest.getEmail().isEmpty())
                throw new RuntimeException("Email cannot be empty or null");
            if(registrationRequest.getPassword() == null || registrationRequest.getPassword().isEmpty())
                throw new RuntimeException("Password cannot be empty or null");

            Invitation invitation = null;
            if(INVITE_ONLY)
                invitation = invitationService.getValidInvitation(registrationRequest.getEmail(), registrationRequest.getInviteToken());

            //Check if the username has already been registered
            if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent())
                throw new RuntimeException("Username already registered");

            //Check if the email has already been registered
            if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent())
                throw new RuntimeException("Email already registered");

            LOGGER.debug("Attempting to build user object");

            //Build the user object to be saved to the database
            User user = new User(registrationRequest.getUsername(), registrationRequest.getEmail(), Role.USER,
                    true, new LinkedHashSet<>(), passwordEncoder.encode(registrationRequest.getPassword()));

            if(emailserviceIntegration) {
                user.setEnabled(false);

                //Send an email so the user can enable their account
                LOGGER.debug("Attempting to send registration email");
                ResponseEntity<?> response = emailUtility.sendEmail(registrationRequest.getEmail(), EmailType.REGISTER);

                if(response.getStatusCode().value() != 200)
                    throw new RestClientException("Unable to send registration email");
            }

            //Save the user to the database
            LOGGER.debug("Saving user to database");
            userRepository.save(user);
            if(INVITE_ONLY)
                invitationService.useInvitation(invitation);
            // Send a telemetry event for user registration
            telemetryUtility.sendTelemetryEvent("user-register", Map.of("userId", user.getId()));

            //Return a 200 response with a JWT token
            LOGGER.info("User successfully created");
            return ResponseEntity.ok(new RegistrationResponse(""));
        } catch(RestClientException ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            LOGGER.error("Unable to send registration email: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(503).body(new ErrorResponse("Unable to send registration email. Please try again later."));
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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
            // Send a telemetry event for user authentication
            telemetryUtility.sendTelemetryEvent("user-authenticate", Map.of("userId", user.getId()));

            //Return a 200 response with the JWT refresh token
            LOGGER.info("Authentication successful");
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtService.buildTokenCookie("refresh_token", refreshToken, true).toString())
                    .body(new AuthenticationResponse());
        } catch (AuthenticationException ex) {
            LOGGER.error("Failed to authenticate user: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(401).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
