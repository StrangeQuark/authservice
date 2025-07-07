package com.strangequark.authservice.auth;

/**
 * Request object for authentication requests
 */
public class AuthenticationRequest {

    /**
     * Username of authenticating user
     */
    private String username;

    /**
     * Password of authenticating user
     */
    private String password;

    /**
     * Constructs a new {@code AuthenticationRequest} with the given dependencies.
     *
     * @param username Username of authenticating user
     * @param password Password of authenticating user
     */
    public AuthenticationRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Get username from authentication request
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username for authentication request
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get password from authentication request
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password for authentication request
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
