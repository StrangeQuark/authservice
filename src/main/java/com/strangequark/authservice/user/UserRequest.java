package com.strangequark.authservice.user;

import java.util.Set;

/**
 * Request object for updating the user's password
 */
public class UserRequest {
    /**
     * Username of the user
     */
    private String username;

    /**
     * Email of the user
     */
    private String email;

    /**
     * Password of authenticating user
     */
    private String password;

    /**
     * New password for update request
     */
    private String newPassword;

    /**
     * Authorizations for update request
     */
    private Set<String> authorizations;

    /**
     * Constructs a new {@code UserRequest} with no dependencies
     */
    public UserRequest() {
    }

    /**
     * Get username for {@link UserRequest} object
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username for {@link UserRequest} object
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get email for {@link UserRequest} object
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set email for {@link UserRequest} object
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get password for {@link UserRequest} object
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password for {@link UserRequest} object
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get newPassword for {@link UserRequest} object
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Set newPassword for {@link UserRequest} object
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * Get {@link Set} of authorizations for {@link UserRequest} object
     */
    public Set<String> getAuthorizations() {
        return authorizations;
    }

    /**
     * Set {@link Set} of authorizations for {@link UserRequest} object
     */
    public void setAuthorizations(Set<String> authorizations) {
        this.authorizations = authorizations;
    }
}