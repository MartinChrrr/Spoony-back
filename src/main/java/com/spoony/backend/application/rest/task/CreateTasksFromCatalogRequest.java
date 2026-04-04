package com.spoony.backend.application.rest.task;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class CreateTasksFromCatalogRequest {

    @NotEmpty(message = "La liste de tâches ne peut pas être vide")
    @Valid
    private List<TaskFromCatalogItem> tasks;

    public CreateTasksFromCatalogRequest() {
    }

    public CreateTasksFromCatalogRequest(List<TaskFromCatalogItem> tasks) {
        this.tasks = tasks;
    }

    public List<TaskFromCatalogItem> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskFromCatalogItem> tasks) {
        this.tasks = tasks;
    }

    public static class TaskFromCatalogItem {

        @NotNull(message = "L'identifiant de la tâche de base est obligatoire")
        private UUID baseTaskId;

        private String name;

        public TaskFromCatalogItem() {
        }

        public TaskFromCatalogItem(UUID baseTaskId, String name) {
            this.baseTaskId = baseTaskId;
            this.name = name;
        }

        public UUID getBaseTaskId() {
            return baseTaskId;
        }

        public void setBaseTaskId(UUID baseTaskId) {
            this.baseTaskId = baseTaskId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
