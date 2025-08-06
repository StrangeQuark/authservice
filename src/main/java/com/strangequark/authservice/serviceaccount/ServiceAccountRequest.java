package com.strangequark.authservice.serviceaccount;

public class ServiceAccountRequest {

    private String clientId;

    private String clientPassword;

    public ServiceAccountRequest(String clientId, String clientPassword) {
        this.clientId = clientId;
        this.clientPassword = clientPassword;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }
}
