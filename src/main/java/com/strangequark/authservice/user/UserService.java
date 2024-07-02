package com.strangequark.authservice.user;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
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

            System.out.println(jwtService.extractUsername(authToken));
            System.out.println(updatePasswordRequest.getPassword());

            //Authenticate the user, throw an AuthenticationException if the username and password combination are incorrect
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            jwtService.extractUsername(authToken),
                            updatePasswordRequest.getPassword()
                    )
            );

            //Get the user, throw an exception if the username is not found
            User user = userRepository.findByUsername(jwtService.extractUsername(authToken))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            System.out.println(user.getPassword());

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
}
