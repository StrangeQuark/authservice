package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.access.AccessService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AccessServiceTest extends BaseServiceTest {

    @Autowired
    AccessService accessService;

    @Test
    void serveAccessTokenTest() {
        {
            //Set the refreshToken to the one stored in the user's DB
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("refresh_token", testUser.getRefreshToken()));
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

            ResponseEntity<?> response =  accessService.serveAccessToken();

            Assertions.assertEquals(200, response.getStatusCode().value());
            Assertions.assertEquals(2, response.getHeaders().get("Set-Cookie").size());
            Assertions.assertTrue(response.getHeaders().get("Set-Cookie").get(0).contains("refresh_token="));
            Assertions.assertTrue(response.getHeaders().get("Set-Cookie").get(1).contains("access_token="));
        }
    }
}
