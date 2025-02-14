package com.strangequark.authservice.user;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.utility.EmailType; // Integration line: Email
import com.strangequark.authservice.utility.EmailUtility; // Integration line: Email
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * {@link Service} for manipulating {@link User} objects
 */
@Service
public class UserService {

    /**
     * {@link UserRepository} for fetching {@link User} from the database
     */
    private final UserRepository userRepository;

    /**
     * {@link JwtService} for extracting the username from the request token
     */
    private final JwtService jwtService;

    /**
     * {@link PasswordEncoder} for encoding our password when updating
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * {@link AuthenticationManager} for authenticating the user
     */
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Business logic updating user's password
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> updatePassword(UserRequest userRequest) {
        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Authenticate the user, throw an AuthenticationException if the username and password combination are incorrect
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            jwtService.extractUsername(authToken, false),
                            userRequest.getPassword()
                    )
            );

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Set the user's new password and save
            user.setPassword(passwordEncoder.encode(userRequest.getNewPassword()));
            userRepository.save(user);

            //Return a 200 response with a success message
            return ResponseEntity.ok(new UserResponse("Password was successfully reset"));

        } catch (AuthenticationException authenticationException) {
            //Throw a 401 (Unauthorized) error if invalid credentials are given
            return ResponseEntity.status(401).body(
                    new ErrorResponse("Invalid password")
            );
        }
    }

    /**
     * Business logic adding authorities to a user
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> addAuthorizations(Set<String> authorizations) {
        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Append the authorizations and save
            user.appendAuthorizations(authorizations);
            userRepository.save(user);

            //Return a 200 response with a success message
            return ResponseEntity.ok(new UserResponse("Authorizations were successfully added"));

        } catch (Exception ex) {
            return ResponseEntity.status(401).body(
                    new ErrorResponse("There was an error in the request, please contact the system administrator")
            );
        }
    }

    /**
     * Business logic for removing authorities from a user
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> removeAuthorizations(Set<String> authorizations) {
        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Remove the authorizations and save
            user.removeAuthorizations(authorizations);
            userRepository.save(user);

            //Return a 200 response with a success message
            return ResponseEntity.ok(new UserResponse("Authorizations were successfully removed"));

        } catch (Exception ex) {
            return ResponseEntity.status(401).body(
                    new ErrorResponse("There was an error in the request, please contact the system administrator")
            );
        }
    }

    /** Integration function start: Email
     * Business logic for initiating the password reset process
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> verifyUserAndSendPasswordResetEmail(UserRequest request) {
        String credentials = request.getCredentials();

        // Try to find the user by username first, and if not found, by email
        Optional<User> userOptional = userRepository.findByUsername(credentials)
                .or(() -> userRepository.findByEmail(credentials));

        if (userOptional.isPresent()) {
            EmailUtility.sendEmail(userOptional.get().getEmail(), "Password reset", EmailType.PASSWORD_RESET);
            return ResponseEntity.ok(new UserResponse("User is present, email is sent"));
        }

        // Handle the case where neither username nor email exists
        return ResponseEntity.status(404).body(new ErrorResponse("User is not present"));
    } // Integration function end: Email

    /**
     * Business logic for enabling a user
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> enableUser(Map<String, String> requestBody) {
        // Check if the User exists
        Optional<User> userOptional = userRepository.findByEmail(requestBody.get("email"));

        if (userOptional.isPresent()) {
            userOptional.get().setEnabled(true);
            userRepository.save(userOptional.get());
            return ResponseEntity.ok(new UserResponse("User is enabled"));
        }

        // Handle the case where neither username nor email exists
        return ResponseEntity.status(404).body(new ErrorResponse("User is not present"));
    }

    /**
     * Business logic for deleting a user
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> deleteUser(UserRequest userRequest) {
        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Authenticate the user, throw an AuthenticationException if the username and password combination are incorrect
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            jwtService.extractUsername(authToken, false),
                            userRequest.getPassword()
                    )
            );

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Delete the user
            userRepository.delete(user);

            //Return a 200 response with a success message
            return ResponseEntity.ok(new UserResponse("User was deleted"));
        } catch (Exception ex) {
            return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
