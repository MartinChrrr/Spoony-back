package com.spoony.backend.application.rest.tasklog;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public class CreateTaskLogsRequest {

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
