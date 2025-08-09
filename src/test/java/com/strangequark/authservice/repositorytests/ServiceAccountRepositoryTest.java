package com.strangequark.authservice.repositorytests;

import com.strangequark.authservice.serviceaccount.ServiceAccount;
import com.strangequark.authservice.serviceaccount.ServiceAccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class ServiceAccountRepositoryTest extends BaseRepositoryTest {

    @TestConfiguration
    static class PasswordEncoderTestConfiguration {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Autowired
    private ServiceAccountRepository serviceAccountRepository;

    @BeforeEach
    void setup() {
        ServiceAccount serviceAccount = new ServiceAccount();
        serviceAccount.setClientId("testServiceAccount");
        serviceAccount.setClientPassword("testClientPassword");

        testEntityManager.persistAndFlush(serviceAccount);
    }

    @Test
    void findByClientIdTest() {
        Optional<ServiceAccount> sa = serviceAccountRepository.findByClientId("testServiceAccount");

        Assertions.assertTrue(sa.isPresent());
    }
}
