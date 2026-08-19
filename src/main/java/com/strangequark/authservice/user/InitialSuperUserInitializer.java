package com.strangequark.authservice.user;

import com.strangequark.authservice.utility.TelemetryUtility; // Integration line: Telemetry
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map; // Integration line: Telemetry
import java.util.UUID;

@Component
@Profile("!test")
@Order(2)
public class InitialSuperUserInitializer implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitialSuperUserInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final TelemetryUtility telemetryUtility; // Integration line: Telemetry

    public InitialSuperUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                       Environment environment, TelemetryUtility telemetryUtility) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.telemetryUtility = telemetryUtility; // Integration line: Telemetry
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        if(userRepository.count() != 0) {
            if(!userRepository.existsByRole(Role.SUPER))
                LOGGER.error("Users exist but no SUPER user exists. Initial SUPER user will not be created automatically");

            return;
        }

        String username = environment.getProperty("INITIAL_SUPER_USERNAME");
        String password = environment.getProperty("INITIAL_SUPER_PASSWORD");

        if((username == null || username.isBlank()) && (password == null || password.isBlank())) {
            username = "super_" + UUID.randomUUID();
            password = generatePassword();
        } else if(username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new RuntimeException("INITIAL_SUPER_USERNAME and INITIAL_SUPER_PASSWORD must both be configured");
        }

        String credentialsFilePath = environment.getProperty("INITIAL_SUPER_CREDENTIALS_FILE");
        if(credentialsFilePath == null || credentialsFilePath.isBlank())
            throw new RuntimeException("INITIAL_SUPER_CREDENTIALS_FILE must be configured");

        Path credentialsFile = Path.of(credentialsFilePath);
        if(credentialsFile.getParent() == null)
            throw new RuntimeException("INITIAL_SUPER_CREDENTIALS_FILE must include a directory");

        Files.createDirectories(credentialsFile.getParent());
        Files.createFile(credentialsFile, PosixFilePermissions.asFileAttribute(
                PosixFilePermissions.fromString("rw-------")));
        Files.writeString(credentialsFile, "Username: " + username + "\nPassword: " + password + "\n");

        try {
            User user = new User(username, username + "@msinit.local", Role.SUPER,
                    true, new HashSet<>(), passwordEncoder.encode(password));
            userRepository.save(user);
        } catch (Exception ex) {
            Files.deleteIfExists(credentialsFile);
            throw ex;
        }

        // Integration function start: Telemetry
        telemetryUtility.sendTelemetryEvent("super-user-bootstrap", Map.of(
                "userId", userRepository.findByUsername(username).get().getId()
        )); // Integration function end: Telemetry

        LOGGER.warn("****************************************************************");
        LOGGER.warn("Initial SUPER user created");
        LOGGER.warn("Username: {}", username);
        LOGGER.warn("Password: {}", password);
        LOGGER.warn("Credentials have also been written to {}", credentialsFile);
        LOGGER.warn("Change the password and delete the credentials file after signing in");
        LOGGER.warn("****************************************************************");
    }

    private String generatePassword() {
        byte[] passwordBytes = new byte[32];
        new SecureRandom().nextBytes(passwordBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(passwordBytes);
    }
}
