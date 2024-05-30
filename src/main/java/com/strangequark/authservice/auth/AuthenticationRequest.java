package com.strangequark.authservice.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for authentication requests
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    /**
     * Username of authenticating user
     */
    private String username;

    /**
     * Password of authenticating user
     */
    private String password;
}
