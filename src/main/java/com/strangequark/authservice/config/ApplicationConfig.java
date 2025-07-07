package com.strangequark.authservice.config;

import com.strangequark.authservice.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring {@link Configuration} for application settings
 */
@Configuration
public class ApplicationConfig {

    /**
     * {@link UserRepository} for fetching the user from the database
     */
    private final UserRepository userRepository;

    /**
     * Constructs a new {@code ApplicationConfig} with the given dependencies.
     *
     * @param userRepository {@link UserRepository} for processing requests to the User database
     */
    public ApplicationConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * {@link Bean} for overriding the {@link UserDetailsService#loadUserByUsername(String)} method
     * @return {@link org.springframework.security.core.userdetails.UserDetails} for the user,
     *          throw {@link UsernameNotFoundException} if the user cannot be found
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * {@link Bean} data access object responsible for fetching
     * {@link org.springframework.security.core.userdetails.UserDetails} and encoding the password
     * @return {@link DaoAuthenticationProvider} with {@link #userDetailsService()} and {@link #passwordEncoder()}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

        //Service to use to fetch information about our user details
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());

        //Provide the password encoder
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

        return daoAuthenticationProvider;
    }

    /**
     * {@link Bean} for setting the password encoder
     * @return new {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * {@link Bean} for managing the username and password authentication
     * @param authenticationConfiguration
     * @return {@link AuthenticationManager} from {@link AuthenticationConfiguration}
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
