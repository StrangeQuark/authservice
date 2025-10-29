package com.strangequark.authservice.bootstrap;

import com.strangequark.authservice.auth.AuthenticationResponse;
import com.strangequark.authservice.auth.RegistrationRequest;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import com.strangequark.authservice.utility.TelemetryUtility; // Integration line: Telemetry
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Integration line: Telemetry
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;

@Service
public class BootstrapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapService.class);

    @Value("${BOOTSTRAP_SECRET_KEY}")
    private String bootstrapSecretKey;

    private final UserRepository userRepository;

    /**
     * {@link PasswordEncoder} for encoding our password when registering a new user to the database
     */
    private final PasswordEncoder passwordEncoder;
    /** Integration function start: Telemetry
     * {@link TelemetryUtility} for sending telemetry events to the Kafka
     */
    @Autowired
    TelemetryUtility telemetryUtility;
    // Integration function end: Telemetry

    public BootstrapService(UserRepository userRepository, PasswordEncoder passwordEncoder)  {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> bootstrap(String headerSecret, RegistrationRequest registrationRequest) {
        LOGGER.info("Attempting to bootstrap admin user");

        try {
            //Check if bootstrap secret is valid
            if (!bootstrapSecretKey.equals(headerSecret))
                throw new RuntimeException("Invalid bootstrap secret");

            //Check if a SUPER user already exists
            if (userRepository.existsByRole(Role.SUPER))
                throw new RuntimeException("At least 1 super user already exists");

            //Check if the username has already been registered
            if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent())
                throw new RuntimeException("Username already registered");

            //Check if the email has already been registered
            if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent())
                throw new RuntimeException("Email address already registered");

            //Build the user object to be saved to the database
            LOGGER.info("Attempting to build bootstrap user object");
            User user = new User(registrationRequest.getUsername(), registrationRequest.getEmail(), Role.SUPER,
                    true, new LinkedHashSet<>(), passwordEncoder.encode(registrationRequest.getPassword()));


            //Save the user to the database
            LOGGER.info("Saving bootstrap user to database");
            userRepository.save(user);
            // Send a telemetry event for super user bootstrap - Integration line: Telemetry
            telemetryUtility.sendTelemetryEvent("super-user-bootstrap", user.getId(), null); // Integration line: Telemetry

            //Return a 200 response with a jwt token
            LOGGER.info("User successfully bootstrapped");
            return ResponseEntity.ok(new AuthenticationResponse());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
