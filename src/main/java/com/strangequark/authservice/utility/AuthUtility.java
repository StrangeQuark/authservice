package com.strangequark.authservice.utility;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.serviceaccount.ServiceAccount;
import com.strangequark.authservice.serviceaccount.ServiceAccountRepository;
import com.strangequark.authservice.serviceaccount.ServiceAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthUtility {
    /**
     * {@link Logger} for writing {@link AuthUtility} application logs
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthUtility.class);

    private final ServiceAccountRepository serviceAccountRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    /**
     * Secret for Auth service account
     */
    @Value("${SERVICE_SECRET_AUTH}")
    private String SERVICE_SECRET_AUTH;

    public AuthUtility(ServiceAccountRepository serviceAccountRepository, PasswordEncoder passwordEncoder,
                                 JwtService jwtService) {
        this.serviceAccountRepository = serviceAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String authenticateServiceAccount() {
        try {
            LOGGER.debug("Attempting to authenticate service account");

            ServiceAccountRequest serviceAccountRequest = new ServiceAccountRequest("auth", SERVICE_SECRET_AUTH);

            //Get the service account, throw an exception if the clientId is not found
            ServiceAccount serviceAccount = serviceAccountRepository.findByClientId(serviceAccountRequest.getClientId())
                    .orElseThrow(() -> new UsernameNotFoundException("Service account not found"));

            if(!passwordEncoder.matches(serviceAccountRequest.getClientPassword(), serviceAccount.getClientPassword()))
                throw new BadCredentialsException("Invalid service account credentials");

            LOGGER.debug("Service account found, creating access token");

            //Create a JWT token to authenticate the service account
            String accessToken = jwtService.generateServiceAccountToken(serviceAccount, false);

            if(accessToken == null)
                throw new RuntimeException("jwtToken not found in authentication response");

            LOGGER.debug("Service account authentication success");
            return accessToken;
        } catch (Exception ex) {
            LOGGER.error("Failed to authenticate service account: " + ex.getMessage());
            LOGGER.debug("Stack trace: ", ex);
            return null;
        }
    }
}
