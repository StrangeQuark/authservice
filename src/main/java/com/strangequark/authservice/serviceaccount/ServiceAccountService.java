package com.strangequark.authservice.serviceaccount;

import com.strangequark.authservice.auth.AuthenticationResponse;
import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ServiceAccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAccountService.class);

    private final ServiceAccountRepository serviceAccountRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public ServiceAccountService(ServiceAccountRepository serviceAccountRepository, PasswordEncoder passwordEncoder,
                                 JwtService jwtService) {
        this.serviceAccountRepository = serviceAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ResponseEntity<?> authenticate(ServiceAccountRequest serviceAccountRequest) {
        LOGGER.info("Attempting to authenticate service account request");

        try {
            //Get the service account, throw an exception if the clientId is not found
            ServiceAccount serviceAccount = serviceAccountRepository.findByClientId(serviceAccountRequest.getClientId())
                    .orElseThrow(() -> new UsernameNotFoundException("Service account not found"));

            if(!passwordEncoder.matches(serviceAccountRequest.getClientPassword(), serviceAccount.getClientPassword()))
                throw new BadCredentialsException("Invalid service account credentials");

            LOGGER.info("Service account found, creating access token");

            //Create a JWT token to authenticate the service account
            String accessToken = jwtService.generateServiceAccountToken(serviceAccount, false);

            //Return a 200 response with the JWT refresh token
            LOGGER.info("Authentication successful");
            return ResponseEntity.ok(new AuthenticationResponse(accessToken));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return ResponseEntity.status(401).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
