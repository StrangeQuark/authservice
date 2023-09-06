package com.strangequark.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring {@link Configuration} for implementing and injecting all declared beans upon startup
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * {@link JwtAuthenticationFilter} to authenticate our JWT tokens and update the security context
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * {@link AuthenticationProvider}
     */
    private final AuthenticationProvider authenticationProvider;

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
                .authorizeHttpRequests().requestMatchers("/api/v1/auth/**", "api/v1/health/**").permitAll()//List of strings (URLs) which are whitelisted and don't need to be authenticated
                .anyRequest().authenticated()//All other requests need to be authenticated
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)//Spring will create a new session for each request
                .and()
                .authenticationProvider(authenticationProvider)//Will explain in a few seconds
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
