package com.strangequark.authservice.user;

import java.util.Set;

/**
 * Request object for updating the user's password
 */
public class UserRequest {
    /**
     * Username or email of the user
     */
    private String credentials;

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
     * Constructs a new {@code UserRequest} with the given dependencies.
     *
     * @param credentials Email address or username for the user request
     */
    public UserRequest(String credentials) {
        this.credentials = credentials;
    }

    /**
     * Constructs a new {@code UserRequest} with the given dependencies.
     *
     * @param credentials Email address or username for the user request
     * @param password Password for the user request
     */
    public UserRequest(String credentials, String password) {
        this(credentials);
        this.password = password;
    }

    /**
     * Constructs a new {@code UserRequest} with the given dependencies.
     *
     * @param credentials Email address or username for the user request
     * @param password Password for the user request
     * @param newPassword New password for update-password user request
     */
    public UserRequest(String credentials, String password, String newPassword) {
        this(credentials, password);
        this.newPassword = newPassword;
    }

    /**
     * Constructs a new {@code UserRequest} with the given dependencies.
     *
     * @param credentials Email address or username for the user request
     * @param authorizations {@link Set} of authorizations for update-authorizations user request
     */
    public UserRequest(String credentials, Set<String> authorizations) {
        this(credentials);
        this.authorizations = authorizations;
    }

    /**
     * Get credentials (Email or username) for {@link UserRequest} object
     */
    public String getCredentials() {
        return credentials;
    }

    /**
     * Set credentials (Email or username) for {@link UserRequest} object
     */
    public void setCredentials(String credentials) {
        this.credentials = credentials;
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