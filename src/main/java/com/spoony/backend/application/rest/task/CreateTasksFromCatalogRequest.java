package com.spoony.backend.application.rest.task;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public class CreateTasksFromCatalogRequest {

    @NotEmpty(message = "La liste des tâches du catalogue est obligatoire")
    private List<UUID> baseTaskIds;

    public CreateTasksFromCatalogRequest() {
    }

    public CreateTasksFromCatalogRequest(List<UUID> baseTaskIds) {
        this.baseTaskIds = baseTaskIds;
    }

    public List<UUID> getBaseTaskIds() {
        return baseTaskIds;
    }

    public void setBaseTaskIds(List<UUID> baseTaskIds) {
        this.baseTaskIds = baseTaskIds;
    }
}
