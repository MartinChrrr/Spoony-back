package com.spoony.backend.application.rest.tasklog;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateManualTaskLogRequest {

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
