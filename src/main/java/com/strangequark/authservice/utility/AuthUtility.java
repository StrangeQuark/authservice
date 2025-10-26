package com.strangequark.authservice.utility;

import com.strangequark.authservice.auth.AuthenticationResponse;
import com.strangequark.authservice.serviceaccount.ServiceAccountRequest;
import com.strangequark.authservice.serviceaccount.ServiceAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthUtility {
    /**
     * {@link Logger} for writing {@link EmailUtility} application logs
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthUtility.class);

    /**
     * Secret for Auth service account
     */
    @Value("${SERVICE_SECRET_AUTH}")
    private String SERVICE_SECRET_AUTH;

    /**
     * For authenticating Auth service account
     */
    @Autowired
    private ServiceAccountService serviceAccountService;

    public String authenticateServiceAccount() {
        try {
            LOGGER.info("Attempting to authenticate service account");

            ServiceAccountRequest request = new ServiceAccountRequest("auth", SERVICE_SECRET_AUTH);

            AuthenticationResponse response = (AuthenticationResponse) serviceAccountService.authenticate(request).getBody();

            String token = response.getJwtToken();

            if(token == null)
                throw new RuntimeException("jwtToken not found in authentication response");

            LOGGER.info("Service account authentication success");
            return token;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
}
