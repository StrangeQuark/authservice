package com.strangequark.authservice.repositorytests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public abstract class BaseRepositoryTest {

    @Autowired
    public TestEntityManager testEntityManager;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Value("${ENCRYPTION_KEY}")
    public String encryptionKey;

    @BeforeAll
    void setupEncryptionKey() {
        System.setProperty("ENCRYPTION_KEY", encryptionKey);
    }
}
