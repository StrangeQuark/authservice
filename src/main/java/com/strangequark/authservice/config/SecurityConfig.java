package com.strangequark.authservice.config;

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
                .authorizeHttpRequests().requestMatchers("/api/auth/register", "/api/auth/authenticate", "/api/auth/health/**", "/api/auth/user/send-password-reset-email", "/api/auth/user/enable-user").permitAll()//List of strings (URLs) which are whitelisted and don't need to be authenticated
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

    //Configure the CORS policy, allow the ReactService through
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                //Allow the reactService through the CORS policy
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://react-service:6000"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true);
            }
        };
    }
}
