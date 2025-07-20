package com.strangequark.authservice.bootstrap;

import com.strangequark.authservice.auth.AuthenticationResponse;
import com.strangequark.authservice.auth.RegistrationRequest;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public BootstrapService(UserRepository userRepository, PasswordEncoder passwordEncoder)  {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> bootstrap(String headerSecret, RegistrationRequest registrationRequest) {
        LOGGER.info("Attempting to bootstrap admin user");

        if(!bootstrapSecretKey.equals(headerSecret)) {
            return ResponseEntity.status(403).body("Invalid bootstrap secret");
        }

        if (userRepository.existsByRole(Role.SUPER)) {
            return ResponseEntity.status(409).body("At least 1 super user already exists");
        }

        //Check if the username has already been registered
        if(userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            LOGGER.error("Username already registered with that bootstrap username");
            return ResponseEntity.status(409).body(
                    new ErrorResponse("Username already registered", 410)
            );
        }

        //Check if the email has already been registered
        if(userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            LOGGER.error("Email address already registered with that bootstrap  email address");
            return ResponseEntity.status(409).body(
                    new ErrorResponse("Email already registered", 401)
            );
        }

        LOGGER.info("Attempting to build bootstrap user object");

        //Build the user object to be saved to the database
        User user = new User(registrationRequest.getUsername(), registrationRequest.getEmail(), Role.SUPER,
                true, new LinkedHashSet<>(), passwordEncoder.encode(registrationRequest.getPassword()));


        //Save the user to the database
        LOGGER.info("Saving bootstrap user to database");
        userRepository.save(user);

        LOGGER.info("User successfully bootstrapped");
        return ResponseEntity.ok(new AuthenticationResponse());
    }
}
