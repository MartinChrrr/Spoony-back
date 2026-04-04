package com.spoony.backend.application.auth;

import java.util.UUID;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String firstName;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, UUID userId, String firstName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.firstName = firstName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
