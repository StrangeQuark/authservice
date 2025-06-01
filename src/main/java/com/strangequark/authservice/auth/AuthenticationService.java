package com.strangequark.authservice.auth;

import com.strangequark.authservice.access.AccessService;
import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import com.strangequark.authservice.utility.EmailType; // Integration line: Email
import com.strangequark.authservice.utility.EmailUtility; // Integration line: Email
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * {@link Service} for registering and authenticating user requests
 */
@Service
public class AuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * Defines whether test run or not
     */
    @Value("${isTestRun}")
    private boolean isTestRun;

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

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Business logic for registering a new user
     * @param registrationRequest
     * @return {@link ResponseEntity} with a {@link AuthenticationResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> register(RegistrationRequest registrationRequest) {
        LOGGER.info("Attempting to register user");

        //Check if the username has already been registered
        if(userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            LOGGER.error("Username already registered with that username");
            return ResponseEntity.status(409).body(
                    new ErrorResponse("Username already registered", 410)
            );
        }

        //Check if the email has already been registered
        if(userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            LOGGER.error("Email address already registered with that email address");
            return ResponseEntity.status(409).body(
                    new ErrorResponse("Email already registered", 401)
            );
        }

        try {
            LOGGER.info("Attempting to build user object");

            //Build the user object to be saved to the database
            User user = new User(registrationRequest.getUsername(), registrationRequest.getEmail(), Role.USER,
                    false, new LinkedHashSet<>(), passwordEncoder.encode(registrationRequest.getPassword()));

            //Send an email so the user can enable their account   -   Integration line: Email
            if(!isTestRun) // Integration line: Email
                EmailUtility.sendEmail(registrationRequest.getEmail(), "Account registration", EmailType.REGISTER); // Integration line: Email

            //Save the user to the database
            userRepository.save(user);

            LOGGER.info("User successfully created");
            return ResponseEntity.ok(new AuthenticationResponse());
        } catch (ResourceAccessException resourceAccessException) {
            LOGGER.error("Unable to send email: " + resourceAccessException.getMessage());
            return ResponseEntity.status(401).body(
                    new ErrorResponse("Unable to send email")
            );
        }
    }

    /**
     * Business logic for authenticating a user
     * @param authenticationRequest
     * @return {@link ResponseEntity} with a {@link AuthenticationResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> authenticate(AuthenticationRequest authenticationRequest) {
        try {
            LOGGER.info("Attempting to authenticate request");

            //Authenticate the user, throw an AuthenticationException if the username and password combination are incorrect
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(authenticationRequest.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Create a JWT token to authenticate the user
            String refreshToken = jwtService.generateToken(user, true);

            //Add the refresh token to the user and save
            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            //Return a 200 response with the JWT refresh token
            return ResponseEntity.ok(new AuthenticationResponse(refreshToken));
        } catch (DisabledException disabledException) {
            LOGGER.error("Account is disabled");

            //Throw a 409 (Resource State conflict) error if invalid credentials are given
            return ResponseEntity.status(409).body(
                    new ErrorResponse(disabledException.getMessage())
            );
        } catch (BadCredentialsException badCredentialsException) {
            LOGGER.error("Invalid credentials");

            //Throw a 401 (Unauthorized) error if invalid credentials are given
            return ResponseEntity.status(401).body(
                    new ErrorResponse(badCredentialsException.getMessage())
            );
        }
    }
}
