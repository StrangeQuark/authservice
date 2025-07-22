package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.access.AccessService;
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
            request.addHeader("Authorization", "Bearer " + testUser.getRefreshToken());
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

            ResponseEntity<?> response =  accessService.serveAccessToken();

            Assertions.assertEquals(200, response.getStatusCode().value());
        }
    }
}
