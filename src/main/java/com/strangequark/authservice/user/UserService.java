package com.strangequark.authservice.user;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.serviceaccount.ServiceAccount; // Integration line: Email
import com.strangequark.authservice.serviceaccount.ServiceAccountRepository; // Integration line: Email
import com.strangequark.authservice.utility.EmailType; // Integration line: Email
import com.strangequark.authservice.utility.EmailUtility; // Integration line: Email
import com.strangequark.authservice.utility.FileUtility; // Integration line: File
import com.strangequark.authservice.utility.VaultUtility; // Integration line: Vault
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    // Integration function start: Email
    /**
     * {@link ServiceAccountRepository} for fetching {@link ServiceAccount} from the database
     */
    @Autowired
    private ServiceAccountRepository serviceAccountRepository; // Integration function end: Email

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

    /** Integration function start: File
     * {@link FileUtility} for sending requests to file service
     */
    @Autowired
    FileUtility fileUtility;
    // Integration function end: File
    /** Integration function start: Vault
     * {@link VaultUtility} for sending requests to vault service
     */
    @Autowired
    VaultUtility vaultUtility;
    // Integration function end: Vault
    /** Integration function start: Email
     * {@link EmailUtility} for sending requests to email service
     */
    @Autowired
    EmailUtility emailUtility;
    // Integration function end: Email
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
            return ResponseEntity.ok(new UserResponse("Password successfully updated"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
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
                throw new RuntimeException("Only SUPER users can add authorizations to SUPER users");

            // Only SUPER and ADMIN users can assign roles
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN)
                throw new RuntimeException("Only SUPER or ADMIN users can add authorizations to users");

            //Append the authorizations and save
            user.appendAuthorizations(userRequest.getAuthorizations());
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("Authorization successfully added");
            return ResponseEntity.ok(new UserResponse("Authorizations successfully added"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
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
            LOGGER.info("Authorizations successfully removed");
            return ResponseEntity.ok(new UserResponse("Authorizations successfully removed"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
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
                emailUtility.sendAsyncEmail(user.getEmail(), "Password reset", EmailType.PASSWORD_RESET);
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
    }

    /**
     * Business logic for resetting a user's password
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> resetPassword(UserRequest userRequest) {
        LOGGER.info("Attempting to reset user's password");

        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            ServiceAccount requestingServiceAccount = serviceAccountRepository.findByClientId(jwtService.extractUsername(authToken, false))
                    .orElseThrow(() -> new UsernameNotFoundException("Requesting service account not found"));

            if(!requestingServiceAccount.getClientId().equals("email"))
                throw new RuntimeException("Only the EMAIL service account can send reset password requests");

            //Get the target user, throw an exception if the email is not found
            User user = userRepository.findByEmail(userRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

            user.setPassword(passwordEncoder.encode(userRequest.getNewPassword()));
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("Password reset success");
            return ResponseEntity.ok(new UserResponse("Password reset success"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }
    // Integration function end: Email
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
            if(userOptional.get().isEnabled()) {
                LOGGER.error("User is already enabled");
                return ResponseEntity.status(400).body(new ErrorResponse("User is already enabled"));
            }

            userOptional.get().setEnabled(true);
            userRepository.save(userOptional.get());

            LOGGER.info("User has been enabled");
            return ResponseEntity.ok(new UserResponse("User has been enabled"));
        }

        // Handle the case where neither username nor email exists
        LOGGER.error("User not found for those credentials when attempting to enable");
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

            if(!user.isEnabled())
                throw new RuntimeException("User is already disabled");

            // Throw error if the target user is a SUPER user
            if(user.getRole() == Role.SUPER)
                throw new RuntimeException("Super users cannot be disabled");

            // If the target user is an ADMIN user, ensure the requesting user is either the target user or a SUPER user
            if(user.getRole() == Role.ADMIN && requestingUser.getRole() != Role.SUPER)
                if(!requestingUser.getId().equals(user.getId()))
                    throw new RuntimeException("ADMIN users can only be self disabled or by a SUPER user");

            // If the requesting user is not SUPER, ADMIN, or self, don't allow users to disable each other
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(user.getId()))
                throw new RuntimeException("Users can only be disabled by self, ADMIN, or SUPER users");

            // Disable the user
            user.setEnabled(false);
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("User has been disabled");
            return ResponseEntity.ok(new UserResponse("User has been disabled"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
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

            // If the requesting user is not SUPER, ADMIN, or self, don't allow users to delete each other
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(user.getId()))
                throw new RuntimeException("Users can only be deleted by self, ADMIN, or SUPER users");

            // Integration function start: File
            LOGGER.info("Attempting to delete user from all File collections");
            try {
                ResponseEntity<?> response = fileUtility.deleteUserFromAllCollections(user.getUsername(), authToken);

                if (response.getStatusCode().value() != 200)
                    throw new RuntimeException("Error when deleting user from fileservice:\n\n" + response.getBody());
            } catch (ResourceAccessException resourceAccessException) {
                //If we are unable to reach the file service, proceed with user deletion
                LOGGER.error("Unable to reach file service: " + resourceAccessException.getMessage());
                LOGGER.info("Skip file deletion - continuing to delete user");
            }// Integration function end: File
            // Integration function start: Vault
            LOGGER.info("Attempting to delete user from all Vault services");
            try {
                ResponseEntity<?> response = vaultUtility.deleteUserFromAllServices(user.getUsername(), authToken);

                if (response.getStatusCode().value() != 200)
                    throw new RuntimeException("Error when deleting user from vaultservice:\n\n" + response.getBody());
            } catch (ResourceAccessException resourceAccessException) {
                //If we are unable to reach the vault service, proceed with user deletion
                LOGGER.error("Unable to reach vault service: " + resourceAccessException.getMessage());
                LOGGER.info("Skip vault deletion - continuing to delete user");
            }// Integration function end: Vault

            //Delete the user
            userRepository.delete(user);

            //Return a 200 response with a success message
            LOGGER.info("User successfully deleted");
            return ResponseEntity.ok(new UserResponse("User successfully deleted"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }

    /**
     * Business logic for updating a user's email
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> updateEmail(UserRequest userRequest) {
        LOGGER.info("Attempting to update user's email address");

        try {
            //Check if the email has already been registered
            if (userRepository.findByEmail(userRequest.getNewEmail()).isPresent())
                throw new RuntimeException("Email already registered");

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
            user.setEmail(userRequest.getNewEmail());
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("Email successfully updated");
            return ResponseEntity.ok(new UserResponse("Email successfully updated"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }

    /**
     * Business logic for updating a user's username
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> updateUsername(UserRequest userRequest) {
        LOGGER.info("Attempting to update username for user");

        try {
            // Check if the username has already been registered
            if (userRepository.findByUsername(userRequest.getNewUsername()).isPresent())
                throw new RuntimeException("Username already registered");

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
            user.setUsername(userRequest.getNewUsername());

            //Create a JWT token to authenticate the user
            String refreshToken = jwtService.generateToken(user, true);

            //Add the refresh token to the user and save
            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("Successfully updated username");
            return ResponseEntity.ok(new UpdateUsernameResponse(refreshToken, jwtService.generateToken(user, false)));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
        }
    }

    /**
     * Business logic for updating a user's role
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> updateRole(UserRequest userRequest) {
        LOGGER.info("Attempting to update user's role");

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
                throw new RuntimeException("Only SUPER users can update roles of SUPER users");

            // Only SUPER and ADMIN users can assign roles
            if(requestingUser.getRole() != Role.SUPER && requestingUser.getRole() != Role.ADMIN)
                throw new RuntimeException("Only SUPER or ADMIN users can update roles");

            //Append the authorizations and save
            user.setRole(userRequest.getNewRole());
            userRepository.save(user);

            //Return a 200 response with a success message
            LOGGER.info("User role successfully updated");
            return ResponseEntity.ok(new UserResponse("User role successfully updated"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }

    /**
     * Business logic for retrieving a user's ID
     * @return {@link ResponseEntity} with user's ID if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> getUserId(String username) {
        LOGGER.info("Attempting to get user ID");

        try {
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

    /**
     * Business logic for searching for a user based on username or email address
     * @return {@link ResponseEntity} with user's ID if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> searchUsers(String query) {
        LOGGER.info("Attempting to query users");

        try {
            User user = userRepository.findByUsername(query)
                    .or(() -> userRepository.findByEmail(query))
                    .orElseThrow(() -> new RuntimeException("No user exists with that username or email address"));

            UserResponse response = new UserResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());

            LOGGER.info("User search success");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }

    public ResponseEntity<?> getUserDetailsByIds(List<UUID> ids) {
        LOGGER.info("Attempting to get list of user details by ids");

        try {
            List<User> users = userRepository.findByIdIn(ids);

            List<UserResponse> response = users.stream()
                    .map(user -> {
                        UserResponse r = new UserResponse();
                        r.setUserId(user.getId());
                        r.setUsername(user.getUsername());
                        r.setEmail(user.getEmail());
                        return r;
                    }).toList();

            LOGGER.info("List of user details successfully compiled");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
