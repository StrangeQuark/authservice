package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.user.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashSet;
import java.util.Optional;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String accessToken;


    @BeforeEach
    void setup() {
        testUser = new User("testUser", "test@test.com", Role.USER, true, new LinkedHashSet(), passwordEncoder.encode("password"));
        userRepository.save(testUser);

        accessToken = jwtService.generateToken(testUser, false);

        //Set the accessToken to the Authorization header in the requestContextHolder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void updatePasswordTest() {
        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest(testUser.getUsername(), "password", "newPassword");

        ResponseEntity<?> response =  userService.updatePassword(updatePasswordRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Password was successfully reset", ((UserResponse) response.getBody()).getMessage());
    }

    @AfterEach
    void teardown() {
        try {
            userRepository.delete(testUser);
            accessToken = null;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
