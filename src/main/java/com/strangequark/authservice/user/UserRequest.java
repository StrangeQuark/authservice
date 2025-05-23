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

    public UserRequest() {
    }

    public UserRequest(String credentials, String password) {
        this.credentials = credentials;
        this.password = password;
    }

    public UserRequest(String credentials, String password, String newPassword) {
        this(credentials, password);
        this.newPassword = newPassword;
    }

    public UserRequest(String credentials, Set<String> authorizations) {
        this.credentials = credentials;
        this.authorizations = authorizations;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public Set<String> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(Set<String> authorizations) {
        this.authorizations = authorizations;
    }
}