package com.strangequark.authservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthRateLimitFilter.class);

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local requests = redis.call('INCR', KEYS[1]) " +
            "if requests == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
            "return requests",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    @Value("${rate-limit.login.max-requests}")
    private int loginMaxRequests;
    @Value("${rate-limit.login.window-minutes}")
    private int loginWindowMinutes;
    @Value("${rate-limit.register.max-requests}")
    private int registerMaxRequests;
    @Value("${rate-limit.register.window-minutes}")
    private int registerWindowMinutes;
    @Value("${rate-limit.password-reset.max-requests}")
    private int passwordResetMaxRequests;
    @Value("${rate-limit.password-reset.window-minutes}")
    private int passwordResetWindowMinutes;
    @Value("${rate-limit.service-account.max-requests}")
    private int serviceAccountMaxRequests;
    @Value("${rate-limit.service-account.window-minutes}")
    private int serviceAccountWindowMinutes;

    public AuthRateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        RateLimit rateLimit = getRateLimit(request);

        if(rateLimit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String key = "auth-rate-limit:" + request.getRequestURI() + ":" + request.getRemoteAddr();
            Long requests = redisTemplate.execute(RATE_LIMIT_SCRIPT, List.of(key),
                    String.valueOf(rateLimit.windowMinutes * 60));

            if(requests == null || requests > rateLimit.maxRequests) {
                response.setStatus(429);
                return;
            }
        } catch(DataAccessException ex) {
            LOGGER.error("Auth rate limiter is unavailable: " + ex.getMessage());
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Rate limiter unavailable");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimit getRateLimit(HttpServletRequest request) {
        if(!request.getMethod().equals("POST"))
            return null;

        return switch(request.getRequestURI()) {
            case "/api/auth/authenticate" -> new RateLimit(loginMaxRequests, loginWindowMinutes);
            case "/api/auth/register" -> new RateLimit(registerMaxRequests, registerWindowMinutes);
            case "/api/auth/user/send-password-reset-email",
                 "/api/auth/user/reset-password" -> new RateLimit(passwordResetMaxRequests, passwordResetWindowMinutes);
            case "/api/auth/service-account/authenticate" ->
                    new RateLimit(serviceAccountMaxRequests, serviceAccountWindowMinutes);
            default -> null;
        };
    }

    private static class RateLimit {
        private final int maxRequests;
        private final int windowMinutes;

        private RateLimit(int maxRequests, int windowMinutes) {
            this.maxRequests = maxRequests;
            this.windowMinutes = windowMinutes;
        }
    }
}
