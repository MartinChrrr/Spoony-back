package com.spoony.backend.application.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête d'inscription")
public class RegisterRequest {

    @Schema(description = "Adresse email", example = "marie@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @Schema(description = "Mot de passe (8 caractères minimum)", example = "motdepasse123", minLength = 8, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @Schema(description = "Prénom de l'utilisateur", example = "Marie", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    private String firstName;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String firstName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
