package com.spoony.backend.application.rest.tasklog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Requête de création de logs de tâches en bulk")
public class CreateTaskLogsRequest {

    @Schema(description = "Liste des identifiants de tâches utilisateur", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "La liste des tâches est obligatoire")
    @Size(max = 100, message = "Une requête ne peut pas contenir plus de 100 tâches")
    private List<@NotNull(message = "Un identifiant de tâche ne peut pas être nul") UUID> userTaskIds;

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
