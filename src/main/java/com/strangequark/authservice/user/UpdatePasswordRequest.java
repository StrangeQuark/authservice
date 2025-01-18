package com.strangequark.authservice.user;

/**
 * Request object for updating the user's password
 */
public class UpdatePasswordRequest {
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

    public UpdatePasswordRequest(String credentials, String password, String newPassword) {
        this.credentials = credentials;
        this.password = password;
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