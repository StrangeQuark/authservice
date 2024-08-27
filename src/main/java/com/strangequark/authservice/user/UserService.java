package com.strangequark.authservice.user;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.utility.EmailUtility;
import lombok.RequiredArgsConstructor;
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
import java.util.Set;

/**
 * {@link Service} for serving access token
 */
@Service
@RequiredArgsConstructor
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
     * {@link AuthenticationManager} for validating the user
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Business logic updating user's password
     * @return {@link ResponseEntity} with a {@link UserResponse} if successful, otherwise return with an {@link ErrorResponse}
     */
    public ResponseEntity<?> updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Authenticate the user, throw an AuthenticationException if the username and password combination are incorrect
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            jwtService.extractUsername(authToken),
                            updatePasswordRequest.getPassword()
                    )
            );

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            //Set the user's new password and save
            user.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
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

    public ResponseEntity<?> addAuthorizations(Set<String> authorizations) {
        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken))
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

    public ResponseEntity<?> removeAuthorizations(Set<String> authorizations) {
        try {
            String authToken = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                    .getHeader("Authorization").substring(7);

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken))
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

    public ResponseEntity<?> verifyUserAndSendPasswordResetEmail(UpdatePasswordRequest request) {
        String credentials = request.getCredentials();

        // Try to find the user by username first, and if not found, by email
        Optional<User> userOptional = userRepository.findByUsername(credentials)
                .or(() -> userRepository.findByEmail(credentials));

        if (userOptional.isPresent()) {
            String email = userOptional.get().getEmail();
            EmailUtility.sendEmail("http://localhost:6005/sendPasswordResetEmail", email, "Password reset");
            return ResponseEntity.ok(new UserResponse("User is present, email is sent"));
        }

        // Handle the case where neither username nor email exists
        return ResponseEntity.status(404).body(new ErrorResponse("User is not present"));
    }
}
