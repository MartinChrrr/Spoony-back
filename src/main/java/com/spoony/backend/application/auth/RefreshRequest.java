package com.spoony.backend.application.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête de rafraîchissement de token")
public class RefreshRequest {

    @Schema(description = "Token de rafraîchissement", example = "dGhpcyBpcyBhIHJlZnJlc2g...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Le refresh token est obligatoire")
    @Size(max = 4096, message = "Le refresh token est trop long")
    private String refreshToken;

    public RefreshRequest() {
    }

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
