package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.auth.RegistrationRequest;
import com.strangequark.authservice.bootstrap.BootstrapService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class BootstrapServiceTest extends BaseServiceTest {

    @Autowired
    private BootstrapService bootstrapService;

    private String headerSecret = "6F54AFE5EBE3355E3E6932ADA8FB9F00BA1780E80B277AB56F23A5AE806C0660";

    @Test
    void bootstrapTest() {
        RegistrationRequest registrationRequest = new RegistrationRequest(
                "bootstrapUsername",
                "bootstrap@bootstrap.com",
                "bootstrapPassword"
                );

        ResponseEntity<?> response = bootstrapService.bootstrap(headerSecret, registrationRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(userRepository.findByUsername("bootstrapUsername").isPresent());
    }
}
