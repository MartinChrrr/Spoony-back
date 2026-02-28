package com.spoony.backend.application.auth;

import com.spoony.backend.application.rest.common.JSendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentification : inscription, connexion, refresh token")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Crée un compte et retourne les tokens JWT")
    @ApiResponse(responseCode = "201", description = "Inscription réussie")
    @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    public ResponseEntity<JSendResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(JSendResponse.success(response));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie l'utilisateur et retourne les tokens JWT")
    @ApiResponse(responseCode = "200", description = "Connexion réussie")
    @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    public ResponseEntity<JSendResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(JSendResponse.success(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Génère de nouveaux tokens à partir du refresh token")
    @ApiResponse(responseCode = "200", description = "Tokens renouvelés")
    @ApiResponse(responseCode = "401", description = "Refresh token invalide")
    public ResponseEntity<JSendResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(JSendResponse.success(response));
    }
}
