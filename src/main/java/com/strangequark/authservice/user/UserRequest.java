package com.strangequark.authservice.user;

import java.util.Set;

/**
 * Request object for updating the user's details
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
     * New username for update request
     */
    private String newUsername;

    /**
     * New email for update request
     */
    private String newEmail;

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
     * Get newUsername for {@link UserRequest} object
     */
    public String getNewUsername() {
        return newUsername;
    }

    /**
     * Set newUsername for {@link UserRequest} object
     */
    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    /**
     * Get newEmail for {@link UserRequest} object
     */
    public String getNewEmail() {
        return newEmail;
    }

    /**
     * Set newEmail for {@link UserRequest} object
     */
    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
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