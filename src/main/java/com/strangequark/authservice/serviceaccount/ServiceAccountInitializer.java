package com.strangequark.authservice.serviceaccount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ServiceAccountInitializer implements ApplicationRunner {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceAccountInitializer.class);
    private final ServiceAccountRepository serviceAccountRepository;
    private final Environment environment;
    private final PasswordEncoder passwordEncoder;

    public ServiceAccountInitializer(ServiceAccountRepository serviceAccountRepository, Environment environment,
                                     PasswordEncoder passwordEncoder) {
        this.serviceAccountRepository = serviceAccountRepository;
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        LOGGER.info("Initializing service accounts");

        String[] serviceIds = environment.getProperty("SERVICE_ACCOUNTS").split(",");

        for(String serviceId : serviceIds) {
            LOGGER.debug("Initializing service account with ID: " + serviceId);
            String clientPassword = environment.getProperty("SERVICE_SECRET_" + serviceId.trim().toUpperCase());

            ServiceAccount serviceAccount = new ServiceAccount();
            serviceAccount.setClientId(serviceId.trim());
            serviceAccount.setClientPassword(passwordEncoder.encode(clientPassword));

            serviceAccountRepository.save(serviceAccount);
            LOGGER.info("Service account successfully initialized");
        }
    }
}
