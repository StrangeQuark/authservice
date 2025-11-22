package com.strangequark.authservice.serviceaccount;

import com.strangequark.authservice.auth.AuthenticationResponse;
import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.utility.TelemetryUtility; // Integration line: Telemetry
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Integration line: Telemetry
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map; // Integration line: Telemetry

@Service
public class ServiceAccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAccountService.class);

    private final ServiceAccountRepository serviceAccountRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    /** Integration function start: Telemetry
     * {@link TelemetryUtility} for sending telemetry events to the Kafka
     */
    @Autowired
    TelemetryUtility telemetryUtility;
    // Integration function end: Telemetry

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

            LOGGER.debug("Service account found, creating access token");

            //Create a JWT token to authenticate the service account
            String accessToken = jwtService.generateServiceAccountToken(serviceAccount, false);
            // Send a telemetry event for service account authentication - Integration line: Telemetry
            telemetryUtility.sendTelemetryEvent("service-account-authenticate", Map.of("serviceAccountId", serviceAccount.getId())); // Integration line: Telemetry

            //Return a 200 response with the JWT refresh token
            LOGGER.info("Service account authentication successful");
            return ResponseEntity.ok(new AuthenticationResponse(accessToken));
        } catch (Exception ex) {
            LOGGER.error("Failed to authenticate service account: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return ResponseEntity.status(401).body(new ErrorResponse(ex.getMessage()));
        }
    }
}
