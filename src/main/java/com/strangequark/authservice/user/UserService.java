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
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Set the user's new password and save
            user.setPassword(passwordEncoder.encode(userRequest.getNewPassword()));
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("Password successfully updated");
            return ResponseEntity.ok(new UserResponse("Password was successfully updated"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponse(ex.getMessage()));
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
                    .orElseThrow(() -> new UsernameNotFoundException("Requesting user not found"));

            //Get the target user, throw an exception if the username or email are not found
            User user = userRepository.findByUsername(userRequest.getUsername())
                    .or(() -> userRepository.findByEmail(userRequest.getEmail()))
                    .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

            // If the target user is a SUPER user, ensure the requesting user is also a SUPER user
            if(user.getRole() == Role.SUPER && requestingUser.getRole() != Role.SUPER)
                throw new RuntimeException("Only SUPER users can assign roles to SUPER users");

            // Only SUPER and ADMIN users can assign roles
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN)
                throw new RuntimeException("Only SUPER or ADMIN users can assign roles");

            //Append the authorizations and save
            user.appendAuthorizations(userRequest.getAuthorizations());
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("Authorization successfully added");
            return ResponseEntity.ok(new UserResponse("Authorizations were successfully added"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponse(ex.getMessage()));
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
                    .orElseThrow(() -> new UsernameNotFoundException("Requesting user not found"));

            //Get the target user, throw an exception if the username or email are not found
            User user = userRepository.findByUsername(userRequest.getUsername())
                    .or(() -> userRepository.findByEmail(userRequest.getEmail()))
                    .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

            // If the target user is a SUPER user, ensure the requesting user is the target user
            if(user.getRole() == Role.SUPER && !requestingUser.getId().equals(user.getId()))
                throw new RuntimeException("Roles on SUPER users can only be self removed");

            // If the target user is an ADMIN user, ensure the requesting user is either the target user or a SUPER user
            if(user.getRole() == Role.ADMIN && requestingUser.getRole() != Role.SUPER)
                if(!requestingUser.getId().equals(user.getId()))
                    throw new RuntimeException("Roles on ADMIN users can only be self removed or by a SUPER user");

            // If the requesting user is not SUPER, ADMIN, or self, don't allow users to remove authorizations from each other
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(user.getId()))
                throw new RuntimeException("Roles can only be removed by self, ADMIN, or SUPER users");

            //Remove the authorizations and save
            user.removeAuthorizations(userRequest.getAuthorizations());
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("Authorizations were successfully removed");
            return ResponseEntity.ok(new UserResponse("Authorizations were successfully removed"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponse(ex.getMessage()));
        }
    }

    /** Integration function start: Email
     * Business logic for initiating the password reset email process
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> sendPasswordResetEmail(UserRequest userRequest) {
        LOGGER.info("Attempting to verify user and send password reset email");

        try {
            //Get the target user, throw an exception if the username or email are not found
            User user = userRepository.findByUsername(userRequest.getUsername())
                    .or(() -> userRepository.findByEmail(userRequest.getEmail()))
                    .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

            try {
                EmailUtility.sendAsyncEmail(user.getEmail(), "Password reset", EmailType.PASSWORD_RESET);
            } catch (Exception ex) {
                LOGGER.error("Unable to send password reset email to kafka");
                LOGGER.error(ex.getMessage());
                return ResponseEntity.status(500).body(new ErrorResponse("Unable to send password reset email"));
            }

            //Return a 200 response with a success message
            LOGGER.info("Password reset email has been sent");
            return ResponseEntity.ok(new UserResponse("Email is sent"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
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
                    .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

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

            //Return a 200 response with a success message
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
                    .orElseThrow(() -> new UsernameNotFoundException("Requesting user not found"));

            //Get the target user, throw an exception if the username or email are not found
            User user = userRepository.findByUsername(userRequest.getUsername())
                    .or(() -> userRepository.findByEmail(userRequest.getEmail()))
                    .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

            // If the target user is a SUPER user, ensure the requesting user is the target user
            if(user.getRole() == Role.SUPER && !requestingUser.getId().equals(user.getId()))
                throw new RuntimeException("SUPER users can only be self-deleted");

            // If the target user is an ADMIN user, ensure the requesting user is either the target user or a SUPER user
            if(user.getRole() == Role.ADMIN && requestingUser.getRole() != Role.SUPER)
                if(!requestingUser.getId().equals(user.getId()))
                    throw new RuntimeException("ADMIN users can only be self-deleted or by a SUPER user");

            // If the requesting user is not SUPER, ADMIN, or self, don't allow users to remove authorizations from each other
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(user.getId()))
                throw new RuntimeException("Users can only be deleted by self, ADMIN, or SUPER users");

            //Delete the user
            userRepository.delete(user);

            //Return a 200 response with a success message
            LOGGER.info("User was successfully deleted");
            return ResponseEntity.ok(new UserResponse("User was successfully deleted"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponse(ex.getMessage()));
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
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Update the user's email
            user.setEmail(userRequest.getEmail());
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("Email successfully updated");
            return ResponseEntity.ok(new UserResponse("Email successfully updated"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponse(ex.getMessage()));
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
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Update the user's username
            user.setUsername(userRequest.getUsername());

            //Create a JWT token to authenticate the user
            String refreshToken = jwtService.generateToken(user, true);

            //Add the refresh token to the user and save
            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("Succesfully updated username for user");
            return ResponseEntity.ok(new UpdateUsernameResponse(refreshToken, jwtService.generateToken(user, false)));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
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

            //Return a 200 response with the user's ID
            LOGGER.info("User Id retrieval success");
            return ResponseEntity.ok(user.getId());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
