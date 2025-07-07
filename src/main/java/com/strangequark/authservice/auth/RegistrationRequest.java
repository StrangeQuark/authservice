package com.strangequark.authservice.auth;

/**
 * Request object for registration requests
 */
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

    /**
     * Constructs a new {@code RegistrationRequest} with the given dependencies.
     *
     * @param username Username of registering user
     * @param email Email address of registering user
     * @param password Password of registering user
     */
    public RegistrationRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Get username from registration request
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username for registration request
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get email address from registration request
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set email for registration request
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get password from registration request
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password for registration request
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
