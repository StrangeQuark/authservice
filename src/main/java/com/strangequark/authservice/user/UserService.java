package com.strangequark.authservice.user;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.utility.EmailType; // Integration line: Email
import com.strangequark.authservice.utility.EmailUtility; // Integration line: Email
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

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
        LOGGER.info("Attempting to update password");

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
                    .orElseThrow(() -> {
                        LOGGER.error("Unable to find user from JWT: " + authToken);
                        return new UsernameNotFoundException("User not found");
                    });

            //Set the user's new password and save
            user.setPassword(passwordEncoder.encode(userRequest.getNewPassword()));
            userRepository.save(user);

            LOGGER.info("Password successfully updated");

            //Return a 200 response with a success message
            return ResponseEntity.ok(new UserResponse("Password was successfully updated"));

        } catch (NullPointerException exception) {
            LOGGER.error(exception.getMessage());

            return ResponseEntity.status(500).body(
                    new ErrorResponse("NPE - trouble getting the request")
            );
        } catch (AuthenticationException authenticationException) {
            //Throw a 401 (Unauthorized) error if invalid credentials are given
            return ResponseEntity.status(401).body(
                    new ErrorResponse("Invalid credentials")
            );
        }
    }

//    Commented because only admins should be able to add authorizations to users - uncomment to allow users to add auths to themselves
//    /**
//     * Business logic adding authorities to a user
//     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
//     */
//    public ResponseEntity<?> addAuthorizations(Set<String> authorizations) {
//        try {
//            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
//                    .getHeader("Authorization").substring(7);
//
//            //Get the user, throw an exception if the username is not found
//            User user = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//            //Append the authorizations and save
//            user.appendAuthorizations(authorizations);
//            userRepository.save(user);
//
//            //Return a 200 response with a success message
//            return ResponseEntity.ok(new UserResponse("Authorizations were successfully added"));
//
//        } catch (Exception ex) {
//            return ResponseEntity.status(401).body(
//                    new ErrorResponse("There was an error in the request, please contact the system administrator")
//            );
//        }
//    }

    /**
     * Business logic adding authorities to a user
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> addAuthorizationsToUser(UserRequest request) {
        LOGGER.info("Attempting to add authorizations to user");

        try {
            String username = request.getCredentials();

            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User requestingUser = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> {
                        LOGGER.error("Requesting user was not found for JWT: " + authToken);
                        return new UsernameNotFoundException("Requesting user not found");
                    });

            if(requestingUser.getRole() == Role.ADMIN) {
                //Get the user, throw an exception if the username is not found
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> {
                            LOGGER.error("Requested user not found");
                            return new UsernameNotFoundException("User not found");
                        });

                //Append the authorizations and save
                user.appendAuthorizations(request.getAuthorizations());
                userRepository.save(user);

                LOGGER.info("Authorization successfully added");

                //Return a 200 response with a success message
                return ResponseEntity.ok(new UserResponse("Authorizations were successfully added"));
            }

            LOGGER.error("Requesting user is unauthorized to make that request, needs ADMIN role");
            return ResponseEntity.status(403).body(new ErrorResponse("Unauthorized to make that request"));
        } catch (NullPointerException exception) {
            LOGGER.error(exception.getMessage());
            return ResponseEntity.status(500).body(
                    new ErrorResponse("NPE - trouble getting the request")
            );
        }
    }

    /**
     * Business logic for removing authorities from a user
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> removeAuthorizations(Set<String> authorizations) {
        LOGGER.info("Attempting to remove authorizations from self");

        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> {
                        LOGGER.error("User not found for JWT: " + authToken);
                        return new UsernameNotFoundException("User not found");
                    });

            //Remove the authorizations and save
            user.removeAuthorizations(authorizations);
            userRepository.save(user);

            LOGGER.info("Authorizations were successfully removed");
            //Return a 200 response with a success message
            return ResponseEntity.ok(new UserResponse("Authorizations were successfully removed"));

        } catch (NullPointerException exception) {
            LOGGER.error(exception.getMessage());

            return ResponseEntity.status(500).body(
                    new ErrorResponse("NPE - trouble getting the request")
            );
        }
    }

    /** Integration function start: Email
     * Business logic for initiating the password reset process
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> verifyUserAndSendPasswordResetEmail(UserRequest request) {
        LOGGER.info("Attempting to verify user and send password reset email");

        // Try to find the user by username first, and if not found, by email
        Optional<User> userOptional = userRepository.findByUsername(request.getCredentials())
                .or(() -> userRepository.findByEmail(request.getCredentials()));

        if (userOptional.isPresent()) {
            EmailUtility.sendEmail(userOptional.get().getEmail(), "Password reset", EmailType.PASSWORD_RESET);

            LOGGER.info("User is present, password reset email has been sent");

            return ResponseEntity.ok(new UserResponse("User is present, email is sent"));
        }

        LOGGER.error("User not found for those credentials");

        // Handle the case where neither username nor email exists
        return ResponseEntity.status(404).body(new ErrorResponse("User is not present"));
    } // Integration function end: Email

    /**
     * Business logic for enabling a user
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> enableUser(Map<String, String> requestBody) {
        LOGGER.info("Attempting to enable user");

        // Check if the User exists
        Optional<User> userOptional = userRepository.findByEmail(requestBody.get("email"));

        if (userOptional.isPresent()) {
            userOptional.get().setEnabled(true);
            userRepository.save(userOptional.get());

            LOGGER.info("User has been enabled");

            return ResponseEntity.ok(new UserResponse("User is enabled"));
        }

        LOGGER.error("User not found for those credentials when attempting to enable");

        // Handle the case where neither username nor email exists
        return ResponseEntity.status(404).body(new ErrorResponse("User is not present"));
    }

    /**
     * Business logic for deleting a user
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> deleteUser(UserRequest userRequest) {
        LOGGER.info("Attempting to delete user");

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
                    .orElseThrow(() -> {
                        LOGGER.error("User was not found for JWT: " + authToken);
                        return new UsernameNotFoundException("User not found");
                    });

            //Delete the user
            userRepository.delete(user);

            LOGGER.info("User was successfully deleted");

            //Return a 200 response with a success message
            return ResponseEntity.ok(new UserResponse("User was deleted"));
        } catch (NullPointerException exception) {
            LOGGER.error(exception.getMessage());

            return ResponseEntity.status(500).body(
                    new ErrorResponse("NPE - trouble getting the request")
            );
        }
    }

    /**
     * Business logic for updating a user's email
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> updateEmail(UserRequest userRequest) {
        LOGGER.info("Attempting to update user's email address");

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
                    .orElseThrow(() -> {
                        LOGGER.error("User not found with JWT: " + authToken);
                        return new UsernameNotFoundException("User not found");
                    });

            //Update the user's email
            user.setEmail(userRequest.getCredentials());
            userRepository.save(user);

            LOGGER.info("Email was successfully updated for user");

            //Return a 200 response with a success message
            return ResponseEntity.ok(new UserResponse("Email was updated"));
        } catch (NullPointerException exception) {
            LOGGER.error(exception.getMessage());

            return ResponseEntity.status(500).body(
                    new ErrorResponse("NPE - trouble getting the request")
            );
        }
    }

    /**
     * Business logic for updating a user's username
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> updateUsername(UserRequest userRequest) {
        LOGGER.info("Attempting to update username for user");

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
                    .orElseThrow(() -> {
                        LOGGER.error("Unable to find user with JWT: " + authToken);
                        return new UsernameNotFoundException("User not found");
                    });

            //Update the user's username
            user.setUsername(userRequest.getCredentials());

            //Create a JWT token to authenticate the user
            String refreshToken = jwtService.generateToken(user, true);

            //Add the refresh token to the user and save
            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            LOGGER.info("Succesfully updated username for user");

            //Return a 200 response with a success message
            return ResponseEntity.ok(new UpdateUsernameResponse(refreshToken, jwtService.generateToken(user, false)));
        } catch (Exception ex) {
            LOGGER.error("Error processing updateUsername request --- " + ex.getMessage());
            return ResponseEntity.status(404).body(new ErrorResponse("There was an error processing your request"));
        }
    }
}
