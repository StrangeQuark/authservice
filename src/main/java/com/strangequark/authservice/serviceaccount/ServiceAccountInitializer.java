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
                Authorization authorization = authorizationRepository.findByName("EMAIL_API_ACCESS")
                        .orElseGet(() -> authorizationRepository.save(new Authorization("EMAIL_API_ACCESS")));

                serviceAccount.getAuthorizations().add(authorization);
            }

            serviceAccountRepository.save(serviceAccount);
            LOGGER.info("Service account successfully initialized: " + trimmedId);
        }
    }
}
