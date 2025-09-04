package com.strangequark.authservice.repositorytests;

import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

public class UserRepositoryTest extends BaseRepositoryTest {

    @TestConfiguration
    static class PasswordEncoderTestConfiguration {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User("testUser", "test@test.com", Role.USER, true, new HashSet<>(), passwordEncoder.encode("password"));
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

    @Test
    void existsByRoleTest() {
        Assertions.assertTrue(userRepository.existsByRole(Role.USER));
    }

    @Test
    void findByIdInTest() {
        List<UUID> uuidList = new ArrayList<>();
        uuidList.add(testUser.getId());
        uuidList.add(UUID.randomUUID());

        List<User> users = userRepository.findByIdIn(uuidList);

        Assertions.assertEquals(1, users.size());
        Assertions.assertTrue(users.contains(testUser));
    }
}
