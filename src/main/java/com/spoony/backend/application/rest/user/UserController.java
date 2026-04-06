package com.spoony.backend.application.rest.user;

import com.spoony.backend.application.rest.common.JSendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Gestion du compte utilisateur (RGPD)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping("/me")
    @Operation(
            summary = "Supprimer mon compte (RGPD)",
            description = "Supprime définitivement le compte de l'utilisateur connecté et toutes ses données associées "
                    + "(tâches, logs, énergie). Cette action est IRRÉVERSIBLE."
    )
    @ApiResponse(responseCode = "204", description = "Compte supprimé définitivement")
    @ApiResponse(responseCode = "401", description = "Token absent ou invalide")
    public ResponseEntity<Void> deleteMyAccount() {
        UUID userId = getCurrentUserId();
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/export")
    @Operation(
            summary = "Exporter mes données (RGPD)",
            description = "Retourne toutes les données personnelles de l'utilisateur (Art. 15 + 20 RGPD)"
    )
    @ApiResponse(responseCode = "200", description = "Données exportées")
    @ApiResponse(responseCode = "401", description = "Token absent ou invalide")
    public ResponseEntity<JSendResponse<UserExportResponse>> exportMyData() {
        UUID userId = getCurrentUserId();
        UserExportResponse export = userService.exportUserData(userId);
        return ResponseEntity.ok(JSendResponse.success(export));
    }

    private UUID getCurrentUserId() {
        String principal = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return UUID.fromString(principal);
    }
}
