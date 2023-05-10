package com.strangequark.userservice.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for registration requests
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    /**
     * Username of new user
     */
    private String username;

    /**
     * Email of new user
     */
    private String email;

    /**
     * Password of new user
     */
    private String password;
}
