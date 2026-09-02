package com.strangequark.authservice.user;

public class UserLookupRequest {
    private String username;
    private String query;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
