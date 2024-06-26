package com.strangequark.authservice.auth;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * {@link Service} for registering and authenticating user requests
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

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

    /**
     * Business logic for registering a new user
     * @param registrationRequest
     * @return {@link ResponseEntity} with a {@link AuthenticationResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> register(RegistrationRequest registrationRequest) {

        //Check if the username has already been registered
        if(userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(409).body(
                    new ErrorResponse("Username already registered", 410)
            );
        }

        //Check if the email has already been registered
        if(!userRepository.findByEmail(registrationRequest.getEmail()).equals(Optional.empty())) {
            return ResponseEntity.status(409).body(
                    new ErrorResponse("Email already registered", 401)
            );
        }

        //Build the user object to be saved to the database
        User user = User.builder()
                .username(registrationRequest.getUsername())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))//Encode the password before saving to database
                .role(Role.USER)
                .build();

        //Save the user to the database
        userRepository.save(user);

        //Create a JWT token to return with the response
        String refreshToken = jwtService.generateToken(user, true);

        return ResponseEntity.ok(new AuthenticationResponse(refreshToken));
    }

    /**
     * Business logic for authenticating a user
     * @param authenticationRequest
     * @return {@link ResponseEntity} with a {@link AuthenticationResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> authenticate(AuthenticationRequest authenticationRequest) {
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

            //Create a JWT token to authenticate the user
            String refreshToken = jwtService.generateToken(user, true);

            //Return a 200 response with the jwtToken
            return ResponseEntity.ok(new AuthenticationResponse(refreshToken));

        } catch (AuthenticationException authenticationException) {
            //Throw a 401 (Unauthorized) error if invalid credentials are given
            return ResponseEntity.status(401).body(
                    new ErrorResponse("Invalid credentials")
            );
        }
    }
}
