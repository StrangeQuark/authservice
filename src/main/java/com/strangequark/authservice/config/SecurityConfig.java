package com.strangequark.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring {@link Configuration} for security settings
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * {@link JwtAuthenticationFilter} to authenticate our JWT tokens and update the security context
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * {@link AuthenticationProvider} neccessary for the security filter
     */
    private final AuthenticationProvider authenticationProvider;

    /**
     * Array of strings to allow through CORS policy
     */
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Constructs a new {@code SecurityConfig} with the given dependencies.
     *
     * @param jwtAuthenticationFilter {@link JwtAuthenticationFilter} for processing JWT during authentication requests
     * @param authenticationProvider {@link AuthenticationProvider} for performing authentication
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * At application startup, Spring will look for a bean of type {@link SecurityFilterChain}
     * This bean is responsible for configuring all the HTTP security of the application
     * @param httpSecurity
     * @return Stateless {@link HttpSecurity} with {@link #jwtAuthenticationFilter}, {@link #authenticationProvider} and whitelist
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()//Disable CSRF
                .authorizeHttpRequests().requestMatchers("/api/auth/register", "/api/auth/authenticate",
                        "/api/auth/health/**", "/api/auth/internal/bootstrap", "api/auth/service-account/authenticate").permitAll()//List of strings (URLs) which are whitelisted and don't need to be authenticated
                .anyRequest().authenticated()//All other requests need to be authenticated
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)//Spring will create a new session for each request
                .and()
                .cors()
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    /**
     * This bean is responsible for configuring all the CORS security of the application
     * @return {@link WebMvcConfigurer} to specify which methods, headers, and origins to allow reach the API
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                //Allow the reactService through the CORS policy
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "DELETE")
                        .allowCredentials(true)
                        .allowedHeaders("Authorization", "Content-Type")
                        .exposedHeaders("Authorization")
                        .maxAge(3600);
            }
        };
    }
}
