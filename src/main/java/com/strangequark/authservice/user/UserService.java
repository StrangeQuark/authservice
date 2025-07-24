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

import java.util.Optional;

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

    /**
     * Constructs a new {@code UserService} with the given dependencies.
     *
     * @param userRepository {@link UserRepository} for performing transactions on the User database
     * @param passwordEncoder {@link PasswordEncoder} for encoding/decoding passwords in the User database
     * @param jwtService {@link JwtService} for generating JWT tokens
     * @param authenticationManager {@link AuthenticationManager} for authenticating JWT tokens
     */
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

    /**
     * Business logic adding authorities to a user
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> addAuthorizationsToUser(UserRequest userRequest) {
        LOGGER.info("Attempting to add authorizations to user");

        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User requestingUser = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> {
                        LOGGER.error("Requesting user was not found for JWT: " + authToken);
                        return new UsernameNotFoundException("Requesting user not found");
                    });

            //Get the target user, throw an exception if the username or email are not found
            User user = userRepository.findByUsername(userRequest.getUsername())
                    .or(() -> userRepository.findByEmail(userRequest.getEmail()))
                    .orElseThrow(() -> {
                        LOGGER.error("Target user not found");
                        return new UsernameNotFoundException("User not found");
                    });

            // If the target user is a SUPER user, ensure the requesting user is also a SUPER user
            if(user.getRole() == Role.SUPER && requestingUser.getRole() != Role.SUPER) {
                LOGGER.error("Only SUPER users can assign roles to SUPER users");
                return ResponseEntity.status(403).body(new ErrorResponse("Only SUPER users can assign roles to SUPER users"));
            }

            // Only SUPER and ADMIN users can assign roles
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN) {
                LOGGER.error("Only SUPER or ADMIN users can assign roles");
                return ResponseEntity.status(403).body(new ErrorResponse("Only SUPER or ADMIN users can assign roles"));
            }

            //Append the authorizations and save
            user.appendAuthorizations(userRequest.getAuthorizations());
            userRepository.save(user);

            LOGGER.info("Authorization successfully added");

            //Return a 200 response with a success message
            return ResponseEntity.ok(new UserResponse("Authorizations were successfully added"));
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
    public ResponseEntity<?> removeAuthorizations(UserRequest userRequest) {
        LOGGER.info("Attempting to remove authorizations from user");

        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User requestingUser = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> {
                        LOGGER.error("Requesting user was not found for JWT: " + authToken);
                        return new UsernameNotFoundException("Requesting user not found");
                    });

            //Get the target user, throw an exception if the username or email are not found
            User user = userRepository.findByUsername(userRequest.getUsername())
                    .or(() -> userRepository.findByEmail(userRequest.getEmail()))
                    .orElseThrow(() -> {
                        LOGGER.error("Target user not found");
                        return new UsernameNotFoundException("User not found");
                    });

            // If the target user is a SUPER user, ensure the requesting user is the target user
            if(user.getRole() == Role.SUPER && !requestingUser.getId().equals(user.getId())) {
                LOGGER.error("Roles on SUPER users can only be self removed");
                return ResponseEntity.status(403).body(new ErrorResponse("Roles on SUPER users can only be self removed"));
            }

            // If the target user is an ADMIN user, ensure the requesting user is either the target user or a SUPER user
            if(user.getRole() == Role.ADMIN && requestingUser.getRole() != Role.SUPER) {
                if(!requestingUser.getId().equals(user.getId())) {
                    LOGGER.error("Roles on ADMIN users can only be self removed or by a SUPER user");
                    return ResponseEntity.status(403).body(new ErrorResponse("Roles on ADMIN users can only be self removed or by a SUPER user"));
                }
            }

            // If the requesting user is not SUPER, ADMIN, or self, don't allow users to remove authorizations from each other
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(user.getId())) {
                LOGGER.error("Roles can only be removed by self, ADMIN, or SUPER users");
                return ResponseEntity.status(403).body(new ErrorResponse("Roles can only be removed by self, ADMIN, or SUPER users"));
            }

            //Remove the authorizations and save
            user.removeAuthorizations(userRequest.getAuthorizations());
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
     * Business logic for initiating the password reset email process
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> sendPasswordResetEmail(UserRequest request) {
        LOGGER.info("Attempting to verify user and send password reset email");

        // Try to find the user by username first, and if not found, by email
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getEmail()));

        if (userOptional.isPresent()) {
            LOGGER.info("User found, attempting to send email");
            try {
                EmailUtility.sendAsyncEmail(userOptional.get().getEmail(), "Password reset", EmailType.PASSWORD_RESET);
            } catch (Exception ex) {
                LOGGER.error("Unable to send password reset email to kafka");
                LOGGER.error(ex.getMessage());
                return ResponseEntity.status(500).body(new ErrorResponse("Unable to send password reset email"));
            }

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
    public ResponseEntity<?> enableUser(UserRequest userRequest) {
        LOGGER.info("Attempting to enable user");

        // Check if the User exists
        Optional<User> userOptional = userRepository.findByEmail(userRequest.getEmail())
                .or(() -> userRepository.findByUsername(userRequest.getUsername()));

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
     * Business logic for disabling a user
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> disableUser(UserRequest userRequest) {
        LOGGER.info("Attempting to disable user");

        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User requestingUser = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> new UsernameNotFoundException("Requesting user not found"));

            //Get the target user, throw an exception if the username or email are not found
            User user = userRepository.findByUsername(userRequest.getUsername())
                    .or(() -> userRepository.findByEmail(userRequest.getEmail()))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Throw error if the target user is a SUPER user
            if(user.getRole() == Role.SUPER) {
                throw new RuntimeException("Super users cannot be disabled");
            }

            // If the target user is an ADMIN user, ensure the requesting user is either the target user or a SUPER user
            if(user.getRole() == Role.ADMIN && requestingUser.getRole() != Role.SUPER) {
                if(!requestingUser.getId().equals(user.getId())) {
                    throw new RuntimeException("ADMIN users can only be self disabled or by a SUPER user");
                }
            }

            // If the requesting user is not SUPER, ADMIN, or self, don't allow users to remove authorizations from each other
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(user.getId())) {
                throw new RuntimeException("Roles can only be removed by self, ADMIN, or SUPER users");
            }

            // Disable the user
            user.setEnabled(false);
            userRepository.save(user);

            LOGGER.info("User has been disabled");
            return ResponseEntity.ok(new UserResponse("User is disabled"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(403).body(new ErrorResponse(ex.getMessage()));
        }
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
            User requestingUser = userRepository.findByUsername(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> {
                        LOGGER.error("Requesting user was not found for JWT: " + authToken);
                        return new UsernameNotFoundException("Requesting user not found");
                    });

            //Get the target user, throw an exception if the username or email are not found
            User user = userRepository.findByUsername(userRequest.getUsername())
                    .or(() -> userRepository.findByEmail(userRequest.getEmail()))
                    .orElseThrow(() -> {
                        LOGGER.error("Target user not found");
                        return new UsernameNotFoundException("User not found");
                    });

            // If the target user is a SUPER user, ensure the requesting user is the target user
            if(user.getRole() == Role.SUPER && !requestingUser.getId().equals(user.getId())) {
                LOGGER.error("SUPER users can only be self-deleted");
                return ResponseEntity.status(403).body(new ErrorResponse("SUPER users can only be self-deleted"));
            }

            // If the target user is an ADMIN user, ensure the requesting user is either the target user or a SUPER user
            if(user.getRole() == Role.ADMIN && requestingUser.getRole() != Role.SUPER) {
                if(!requestingUser.getId().equals(user.getId())) {
                    LOGGER.error("ADMIN users can only be self-deleted or by a SUPER user");
                    return ResponseEntity.status(403).body(new ErrorResponse("ADMIN users can only be self-deleted or by a SUPER user"));
                }
            }

            // If the requesting user is not SUPER, ADMIN, or self, don't allow users to remove authorizations from each other
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(user.getId())) {
                LOGGER.error("Users can only be deleted by self, ADMIN, or SUPER users");
                return ResponseEntity.status(403).body(new ErrorResponse("Users can only be deleted by self, ADMIN, or SUPER users"));
            }

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
            user.setEmail(userRequest.getEmail());
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
            user.setUsername(userRequest.getUsername());

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

    /**
     * Business logic for retrieving a user's ID
     * @return {@link ResponseEntity} with user's ID if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> getUserId(String username) {
        try {
            LOGGER.info("Attempting to get user ID");

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("No user exists with that username"));

            LOGGER.info("User Id retrieval success");
            return ResponseEntity.ok(user.getId());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
