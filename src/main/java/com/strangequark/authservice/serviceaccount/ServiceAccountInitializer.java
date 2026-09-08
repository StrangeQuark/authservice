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

            ServiceAccount serviceAccount = serviceAccountRepository.findByClientId(trimmedId).orElseGet(() -> {
                String clientPassword = environment.getProperty("SERVICE_SECRET_" + trimmedId.toUpperCase());
                ServiceAccount account = new ServiceAccount();
                account.setClientId(trimmedId);
                account.setClientPassword(passwordEncoder.encode(clientPassword));
                return account;
            });
            String authorizationNames = environment.getProperty("INITIAL_" + trimmedId.toUpperCase() + "_SERVICE_ACCOUNT_AUTHORIZATIONS", "");
            for(String authorizationName : authorizationNames.split(",")) {
                if(!authorizationName.isBlank())
                    addAuthorization(serviceAccount, authorizationName.trim());
            }

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
