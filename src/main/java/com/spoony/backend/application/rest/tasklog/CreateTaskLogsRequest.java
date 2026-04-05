package com.spoony.backend.application.rest.tasklog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(description = "Requête de création de logs de tâches en bulk")
public class CreateTaskLogsRequest {

    @Schema(description = "Liste des identifiants de tâches utilisateur", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "La liste des tâches est obligatoire")
    private List<UUID> userTaskIds;

    public CreateTaskLogsRequest() {
    }

    public CreateTaskLogsRequest(List<UUID> userTaskIds) {
        this.userTaskIds = userTaskIds;
    }

    public List<UUID> getUserTaskIds() {
        return userTaskIds;
    }

    public void setUserTaskIds(List<UUID> userTaskIds) {
        this.userTaskIds = userTaskIds;
    }
}
