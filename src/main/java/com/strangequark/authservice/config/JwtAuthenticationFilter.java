package com.strangequark.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.strangequark.authservice.error.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A filter to be run every time a request is made to the "users" table.
 */

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /**
     * {@link Logger} for writing {@link JwtAuthenticationFilter} application logs
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * {@link JwtService for performing operations on JWT tokens}
     */
    private final JwtService jwtService;

    /**
     * {@link UserDetailsService for performing operations on a user}
     */
    private final UserDetailsService userDetailsService;

    /**
     * Constructs a new {@code JwtAuthenticationFilter} with the given dependencies.
     *
     * @param jwtService {@link JwtService} for performing operations on the JWT in the request
     * @param userDetailsService {@link UserDetailsService} for loading user details
     */
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Perform the following operations in sequential order:
     *      -Check if the authorization header is null or does not start with "Bearer ", if true then go to the
     *      next filter and return
     *      -Set the value of jwtToken to the authorizationHeader, exlcuding the "Bearer " substring
     * @param request The request being sent to the server - Cannot be null
     * @param response The response being sent back to the user - Cannot be null
     * @param filterChain List of all the filters to be executed when a request is made - Cannot be null
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            LOGGER.info("Attempting to do an internal filter");

            final String authorizationHeader = request.getHeader("Authorization");
            final String jwtToken;
            final String username;
            final boolean isRefreshToken;

            //Check if the authorizationHeader is null or does not start with "Bearer "
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                LOGGER.error("Invalid authorization header");
                return;
            }

            //Check the path, set refresh or access token
            isRefreshToken = request.getRequestURI().equals("/api/auth/access");

            //Insert the authorization header, excluding the "Bearer " substring
            jwtToken = authorizationHeader.substring(7);

            //Extract the username from the JWT token
            username = jwtService.extractUsername(jwtToken, isRefreshToken);
            LOGGER.info("Username successfully extracted from JWT token");

            //SecurityContextHolder.getContext().getAuthentication() == null -> User is not yet authenticated (Connected)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                LOGGER.info("Username and SecurityContextHolder are valid");

                //Load the UserDetails of the user
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                LOGGER.info("Checking that JWT token is valid");
                //Check if the JWT token is valid for the user
                if (jwtService.isTokenValid(jwtToken, userDetails, isRefreshToken)) {
                    LOGGER.info("JWT token confirmed valid");

                    //Create a new authentication token from the UserDetails
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    //Enforce the authentication token with the details from our request
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    //Update the security context holder
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    LOGGER.info("Security context holder updated");
                }
            }

            LOGGER.info("doFilterInternal complete, passing request to next filter");
            //Pass the request to the next filter
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException expiredJwtException) {
            LOGGER.error("JWT is expired when trying to doFilterInteral, sending failure response");

            //Create JSON for response body
            ObjectWriter objectWriter = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
            String responseBody = objectWriter.writeValueAsString(new ErrorResponse("Your JWT refresh token has expired", 4001));

            //Unauthorized status code
            response.setStatus(401);

            //Return error response with message
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(responseBody);
            response.getWriter().flush();
        }
    }
}
