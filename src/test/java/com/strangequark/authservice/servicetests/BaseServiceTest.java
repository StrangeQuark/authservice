package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;

public abstract class BaseServiceTest {

    @Autowired
    public UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    public PasswordEncoder passwordEncoder;
    public User testUser;
    public User testAdmin;
    private String accessToken;

    @BeforeEach
    void setup() {
        HashSet<String> testAuthorizations = new HashSet<>();
        testAuthorizations.add("testAuthorization1");

        testUser = new User("testUser", "test@test.com", Role.USER, true, testAuthorizations, passwordEncoder.encode("password"));
        testUser.setRefreshToken(jwtService.generateToken(testUser, true));
        userRepository.save(testUser);

        accessToken = jwtService.generateToken(testUser, false);

        //Set the accessToken to the Authorization header in the requestContextHolder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void teardown() {
        userRepository.deleteAll();
        accessToken = null;
        testUser = null;
    }

    void setupAdminUser() {
        testAdmin = new User("testAdmin", "admin@test.com", Role.ADMIN, true, new HashSet<>(), passwordEncoder.encode("adminPassword"));
        testAdmin.setRefreshToken(jwtService.generateToken(testAdmin, true));
        userRepository.save(testAdmin);

        accessToken = jwtService.generateToken(testAdmin, false);

        //Set the accessToken to the Authorization header in the requestContextHolder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
