package com.spoony.backend.application.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Réponse d'authentification avec tokens JWT")
public class AuthResponse {

    @Schema(description = "Token d'accès JWT", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Token de rafraîchissement", example = "dGhpcyBpcyBhIHJlZnJlc2g...")
    private String refreshToken;

    @Schema(description = "Identifiant de l'utilisateur", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Prénom de l'utilisateur", example = "Marie")
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
