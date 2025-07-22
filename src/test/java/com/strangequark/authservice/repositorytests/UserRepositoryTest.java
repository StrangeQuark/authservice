package com.strangequark.authservice.repositorytests;

import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Optional;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class UserRepositoryTest {

    @TestConfiguration
    static class PasswordEncoderTestConfiguration {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${ENCRYPTION_KEY}")
    String encryptionKey;

    @BeforeAll
    void setupEncryptionKey() {
        System.setProperty("ENCRYPTION_KEY", encryptionKey);
    }

    @BeforeEach
    void setup() {
        User testUser = new User("testUser", "test@test.com", Role.USER, true, new HashSet<>(), passwordEncoder.encode("password"));
        testEntityManager.persistAndFlush(testUser);
    }

    @Test
    void findByUsernameTest() {
        Optional<User> user = userRepository.findByUsername("testUser");

        Assertions.assertTrue(user.isPresent());
    }

    @Test
    void findByEmailTest() {
        Optional<User> user = userRepository.findByEmail("test@test.com");

        Assertions.assertTrue(user.isPresent());
    }
}
