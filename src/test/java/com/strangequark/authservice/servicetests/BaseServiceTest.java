package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.authorization.Authorization;
import com.strangequark.authservice.authorization.AuthorizationInitializer;
import com.strangequark.authservice.authorization.AuthorizationRepository;
import com.strangequark.authservice.authorization.RoleAuthorizationRepository;
import com.strangequark.authservice.serviceaccount.ServiceAccountRepository; // Integration line: Email
import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public abstract class BaseServiceTest {

    static {
        System.setProperty("ENCRYPTION_KEY", "AA1A2A8C0E4F76FB3C13F66225AAAC42");
        System.setProperty("SERVICE_SECRET_AUTH", "testClientPassword");
        System.setProperty("SERVICE_SECRET_EMAIL", "testEmailPassword");
        System.setProperty("SERVICE_ACCOUNTS", "auth,email");
    }

    @Autowired
    public UserRepository userRepository;
    @Autowired
    protected JwtService jwtService;
    @Autowired
    public AuthorizationRepository authorizationRepository;
    @Autowired
    public RoleAuthorizationRepository roleAuthorizationRepository;
    @Autowired
    public AuthorizationInitializer authorizationInitializer;
    @Autowired
    public PasswordEncoder passwordEncoder;
    public User testUser;
    public User testAdmin;
    public User testSuper;
    private String accessToken;
    @Autowired // Integration line: Email
    protected ServiceAccountRepository serviceAccountRepository; // Integration line: Email

    @BeforeEach
    void setup() {
        authorizationInitializer.run(new DefaultApplicationArguments());

        HashSet<Authorization> testAuthorizations = new HashSet<>();

        testUser = new User("testUser", "test@test.com", Role.USER, true, testAuthorizations, passwordEncoder.encode("password"));
        userRepository.save(testUser);

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
        roleAuthorizationRepository.deleteAll();
        accessToken = null;
        testUser = null;
    }

    void setupAdminUser() {
        testAdmin = new User("testAdmin", "admin@test.com", Role.ADMIN, true, new HashSet<>(), passwordEncoder.encode("adminPassword"));
        userRepository.save(testAdmin);

        testAdmin.setRefreshToken(jwtService.generateToken(testAdmin, true));
        userRepository.save(testAdmin);

        accessToken = jwtService.generateToken(testAdmin, false);

        //Set the accessToken to the Authorization header in the requestContextHolder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    void setupSuperUser() {
        testSuper = new User("testSuper", "super@test.com", Role.SUPER, true, new HashSet<>(), passwordEncoder.encode("superPassword"));
        userRepository.save(testSuper);

        testSuper.setRefreshToken(jwtService.generateToken(testSuper, true));
        userRepository.save(testSuper);

        accessToken = jwtService.generateToken(testSuper, false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
    // Integration function start: Email
    void setupEmailServiceAccount() {
        accessToken = jwtService.generateServiceAccountToken(serviceAccountRepository.findByClientId("email").get(), false);

        //Set the accessToken to the Authorization header in the requestContextHolder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    } // Integration function end: Email
}
