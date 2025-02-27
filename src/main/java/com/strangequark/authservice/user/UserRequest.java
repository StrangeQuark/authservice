package com.strangequark.authservice.user;

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
}