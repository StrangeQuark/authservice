package com.strangequark.authservice.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthRateLimitFilterTest {
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final FilterChain filterChain = mock(FilterChain.class);
    private AuthRateLimitFilter authRateLimitFilter;

    @BeforeEach
    void setup() {
        authRateLimitFilter = new AuthRateLimitFilter(redisTemplate);

        ReflectionTestUtils.setField(authRateLimitFilter, "loginMaxRequests", 2);
        ReflectionTestUtils.setField(authRateLimitFilter, "loginWindowMinutes", 15);
        ReflectionTestUtils.setField(authRateLimitFilter, "registerMaxRequests", 2);
        ReflectionTestUtils.setField(authRateLimitFilter, "registerWindowMinutes", 60);
        ReflectionTestUtils.setField(authRateLimitFilter, "passwordResetMaxRequests", 2);
        ReflectionTestUtils.setField(authRateLimitFilter, "passwordResetWindowMinutes", 60);
        ReflectionTestUtils.setField(authRateLimitFilter, "serviceAccountMaxRequests", 2);
        ReflectionTestUtils.setField(authRateLimitFilter, "serviceAccountWindowMinutes", 1);
    }

    @Test
    void extraLoginRequestReturnsTooManyRequests() throws Exception {
        when(redisTemplate.execute(any(), anyList(), anyString())).thenReturn(1L, 2L, 3L);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/authenticate");
        request.setRemoteAddr("127.0.0.1");

        authRateLimitFilter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);
        authRateLimitFilter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        MockHttpServletResponse response = new MockHttpServletResponse();
        authRateLimitFilter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void nonRateLimitedRequestPassesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/access");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authRateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
