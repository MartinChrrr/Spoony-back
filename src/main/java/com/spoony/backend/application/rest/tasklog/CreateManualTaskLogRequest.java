package com.spoony.backend.application.rest.tasklog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Requête de création manuelle d'un log de tâche")
public class CreateManualTaskLogRequest {

    @Schema(description = "Identifiant de la tâche utilisateur", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "L'identifiant de la tâche est obligatoire")
    private UUID userTaskId;

    public CreateManualTaskLogRequest() {
    }

    public CreateManualTaskLogRequest(UUID userTaskId) {
        this.userTaskId = userTaskId;
    }

    public UUID getUserTaskId() {
        return userTaskId;
    }

    public void setUserTaskId(UUID userTaskId) {
        this.userTaskId = userTaskId;
    }
}
