package com.spoony.backend.application.rest.suggestion;

import com.spoony.backend.application.rest.common.JSendResponse;
import com.spoony.backend.domain.suggestion.port.in.SuggestionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suggestions")
@Tag(name = "Suggestions", description = "Suggestions de tâches basées sur l'énergie et la priorité")
public class SuggestionController {

    private final SuggestionUseCase suggestionUseCase;

    public SuggestionController(SuggestionUseCase suggestionUseCase) {
        this.suggestionUseCase = suggestionUseCase;
    }

    @GetMapping
    @Operation(
            summary = "Obtenir les suggestions du jour",
            description = "Retourne les tâches suggérées triées par score. Si énergie=0, retourne une liste vide."
    )
    @ApiResponse(responseCode = "200", description = "Suggestions retournées")
    @ApiResponse(responseCode = "404", description = "Énergie non déclarée")
    public ResponseEntity<JSendResponse<List<SuggestionResponse>>> getSuggestions() {
        UUID userId = getCurrentUserId();
        List<SuggestionResponse> suggestions = suggestionUseCase.getSuggestions(userId).stream()
                .map(SuggestionResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(JSendResponse.success(suggestions));
    }

    private UUID getCurrentUserId() {
        String principal = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return UUID.fromString(principal);
    }
}
