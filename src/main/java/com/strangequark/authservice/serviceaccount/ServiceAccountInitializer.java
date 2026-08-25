package com.strangequark.authservice.serviceaccount;

import com.strangequark.authservice.authorization.Authorization;
import com.strangequark.authservice.authorization.AuthorizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ServiceAccountInitializer implements ApplicationRunner {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceAccountInitializer.class);
    private final ServiceAccountRepository serviceAccountRepository;
    private final Environment environment;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationRepository authorizationRepository;

    public ServiceAccountInitializer(ServiceAccountRepository serviceAccountRepository, Environment environment,
                                     PasswordEncoder passwordEncoder, AuthorizationRepository authorizationRepository) {
        this.serviceAccountRepository = serviceAccountRepository;
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
        this.authorizationRepository = authorizationRepository;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        LOGGER.info("Initializing service accounts");

        String[] serviceIds = environment.getProperty("SERVICE_ACCOUNTS").split(",");

        for(String serviceId : serviceIds) {
            LOGGER.debug("Attempting to initialize service account with ID: " + serviceId);
            String trimmedId = serviceId.trim();

            if (serviceAccountRepository.findByClientId(trimmedId).isPresent()) {
                LOGGER.debug("Service account already exists: " + trimmedId);
                continue;
            }

            String clientPassword = environment.getProperty("SERVICE_SECRET_" + trimmedId.toUpperCase());
            ServiceAccount serviceAccount = new ServiceAccount();
            serviceAccount.setClientId(trimmedId);
            serviceAccount.setClientPassword(passwordEncoder.encode(clientPassword));
            if(trimmedId.equals("auth")) {
                addAuthorization(serviceAccount, "EMAIL_API_ACCESS"); // Integration line: Email
                addAuthorization(serviceAccount, "TELEMETRY_API_ACCESS"); // Integration line: Telemetry
            }
            // Integration function start: Email
            if(trimmedId.equals("email")) {
                addAuthorization(serviceAccount, "AUTH_API_ACCESS");
                addAuthorization(serviceAccount, "TELEMETRY_API_ACCESS"); // Integration line: Telemetry
            } // Integration function end: Email
            // Integration function start: File
            if(trimmedId.equals("file")) {
                addAuthorization(serviceAccount, "AUTH_API_ACCESS");
                addAuthorization(serviceAccount, "TELEMETRY_API_ACCESS"); // Integration line: Telemetry
            } // Integration function end: File
            // Integration function start: Vault
            if(trimmedId.equals("vault")) {
                addAuthorization(serviceAccount, "AUTH_API_ACCESS");
                addAuthorization(serviceAccount, "TELEMETRY_API_ACCESS"); // Integration line: Telemetry
            } // Integration function end: Vault
            // Integration function start: Test
            if(trimmedId.equals("test")) {
                addAuthorization(serviceAccount, "AUTH_API_ACCESS");
                addAuthorization(serviceAccount, "EMAIL_API_ACCESS"); // Integration line: Email
                addAuthorization(serviceAccount, "FILE_API_ACCESS"); // Integration line: File
                addAuthorization(serviceAccount, "VAULT_API_ACCESS"); // Integration line: Vault
                addAuthorization(serviceAccount, "TELEMETRY_API_ACCESS"); // Integration line: Telemetry
            } // Integration function end: Test

            serviceAccountRepository.save(serviceAccount);
            LOGGER.info("Service account successfully initialized: " + trimmedId);
        }
    }

    private void addAuthorization(ServiceAccount serviceAccount, String name) {
        Authorization authorization = authorizationRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Authorization was not found"));

        serviceAccount.getAuthorizations().add(authorization);
    }
}
