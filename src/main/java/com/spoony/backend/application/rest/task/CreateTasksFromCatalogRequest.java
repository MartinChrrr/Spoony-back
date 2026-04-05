package com.spoony.backend.application.rest.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Requête de création de tâches depuis le catalogue")
public class CreateTasksFromCatalogRequest {

    @Schema(description = "Liste de tâches à créer depuis le catalogue", requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(description = "Élément de tâche à créer depuis le catalogue")
    public static class TaskFromCatalogItem {

        @Schema(description = "Identifiant de la tâche de base du catalogue", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "L'identifiant de la tâche de base est obligatoire")
        private UUID baseTaskId;

        @Schema(description = "Nom personnalisé pour la tâche", example = "Faire les courses", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le nom de la tâche est obligatoire")
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
