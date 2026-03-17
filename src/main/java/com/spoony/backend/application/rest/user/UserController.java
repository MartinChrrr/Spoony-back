package com.spoony.backend.application.rest.user;

import com.spoony.backend.application.rest.common.JSendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
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
    @ApiResponse(responseCode = "200", description = "Compte supprimé définitivement")
    public ResponseEntity<JSendResponse<Map<String, String>>> deleteMyAccount() {
        UUID userId = getCurrentUserId();
        userService.deleteUser(userId);
        return ResponseEntity.ok(JSendResponse.success(Map.of("message", "Compte supprimé définitivement")));
    }

    private UUID getCurrentUserId() {
        String principal = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return UUID.fromString(principal);
    }
}
