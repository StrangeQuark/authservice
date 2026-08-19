package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.user.InitialSuperUserInitializer;
import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.utility.TelemetryUtility;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InitialSuperUserInitializerTest extends BaseServiceTest {
    private final EntityManager entityManager = mock(EntityManager.class);

    @TempDir
    Path testDirectory;

    @BeforeEach
    void setupEntityManager() {
        reset(entityManager);

        Query query = mock(Query.class);
        when(entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(6001)")).thenReturn(query);
    }

    @Test
    void initializeSuperUserTest() throws Exception {
        userRepository.deleteAll();
        Path credentialsFile = testDirectory.resolve("initial-super-user.txt");

        InitialSuperUserInitializer initialSuperUserInitializer = new InitialSuperUserInitializer(
                userRepository, passwordEncoder, getEnvironment(credentialsFile), mock(TelemetryUtility.class),
                entityManager);
        initialSuperUserInitializer.run(new DefaultApplicationArguments());

        String[] credentials = Files.readString(credentialsFile).split("\\n");
        String username = credentials[0].replace("Username: ", "");
        String password = credentials[1].replace("Password: ", "");
        User user = userRepository.findByUsername(username).get();

        Assertions.assertEquals(Role.SUPER, user.getRole());
        Assertions.assertTrue(user.isEnabled());
        Assertions.assertTrue(passwordEncoder.matches(password, user.getPassword()));
        verify(entityManager).createNativeQuery("SELECT pg_advisory_xact_lock(6001)");
    }

    @Test
    void initializeSuperUserWithExistingUserTest() throws Exception {
        Path credentialsFile = testDirectory.resolve("initial-super-user.txt");

        InitialSuperUserInitializer initialSuperUserInitializer = new InitialSuperUserInitializer(
                userRepository, passwordEncoder, getEnvironment(credentialsFile), mock(TelemetryUtility.class),
                entityManager);
        initialSuperUserInitializer.run(new DefaultApplicationArguments());

        Assertions.assertEquals(1, userRepository.count());
        Assertions.assertFalse(Files.exists(credentialsFile));
    }

    @Test
    void initializeSuperUserWithExistingCredentialsFileTest() throws Exception {
        userRepository.deleteAll();
        Path credentialsFile = testDirectory.resolve("initial-super-user.txt");
        Files.writeString(credentialsFile, "existing credentials");

        InitialSuperUserInitializer initialSuperUserInitializer = new InitialSuperUserInitializer(
                userRepository, passwordEncoder, getEnvironment(credentialsFile), mock(TelemetryUtility.class),
                entityManager);

        Assertions.assertThrows(FileAlreadyExistsException.class,
                () -> initialSuperUserInitializer.run(new DefaultApplicationArguments()));
        Assertions.assertEquals(0, userRepository.count());
        Assertions.assertEquals("existing credentials", Files.readString(credentialsFile));
    }

    private MockEnvironment getEnvironment(Path credentialsFile) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("INITIAL_SUPER_CREDENTIALS_FILE", credentialsFile.toString());

        return environment;
    }
}
