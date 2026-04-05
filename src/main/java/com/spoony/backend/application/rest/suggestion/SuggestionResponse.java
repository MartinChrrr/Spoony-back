package com.spoony.backend.application.rest.suggestion;

import com.spoony.backend.domain.suggestion.model.Suggestion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Suggestion de tâche basée sur l'énergie et la priorité")
public class SuggestionResponse {

    @Schema(description = "Identifiant de la tâche utilisateur", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userTaskId;

    @Schema(description = "Nom de la tâche", example = "Faire les courses")
    private String name;

    @Schema(description = "Coût en cuillères", example = "3", minimum = "1", maximum = "5")
    private int spoonCost;

    @Schema(description = "Niveau d'importance", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private String importance;

    @Schema(description = "Clé i18n de la raison de suggestion (format: IMPORTANCE.STATUS[:days=N])", example = "HIGH.NOT_DONE_SINCE:days=5")
    private String reason;

    @Schema(description = "Indique si cette tâche dépasse le budget de cuillères restant", example = "false")
    private boolean exceedsBudget;

    public SuggestionResponse() {
    }

    public static SuggestionResponse fromDomain(Suggestion suggestion) {
        SuggestionResponse response = new SuggestionResponse();
        response.setUserTaskId(suggestion.getUserTaskId());
        response.setName(suggestion.getName());
        response.setSpoonCost(suggestion.getSpoonCost());
        response.setImportance(suggestion.getImportance());
        response.setReason(suggestion.getReason());
        response.setExceedsBudget(suggestion.isExceedsBudget());
        return response;
    }

    public UUID getUserTaskId() {
        return userTaskId;
    }

    public void setUserTaskId(UUID userTaskId) {
        this.userTaskId = userTaskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSpoonCost() {
        return spoonCost;
    }

    public void setSpoonCost(int spoonCost) {
        this.spoonCost = spoonCost;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isExceedsBudget() {
        return exceedsBudget;
    }

    public void setExceedsBudget(boolean exceedsBudget) {
        this.exceedsBudget = exceedsBudget;
    }
}
