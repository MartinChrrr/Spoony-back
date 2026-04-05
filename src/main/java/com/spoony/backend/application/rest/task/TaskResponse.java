package com.spoony.backend.application.rest.task;

import com.spoony.backend.domain.task.model.UserTask;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Tâche utilisateur")
public class TaskResponse {

    @Schema(description = "Identifiant unique de la tâche", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Nom de la tâche", example = "Faire les courses")
    private String name;

    @Schema(description = "Coût en cuillères", example = "3", minimum = "1", maximum = "5")
    private int spoonCost;

    @Schema(description = "Niveau d'importance", example = "MEDIUM", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private String importance;

    @Schema(description = "Catégorie de la tâche", example = "household")
    private String category;

    @Schema(description = "Date d'échéance", example = "2026-04-05")
    private LocalDate dueDate;

    @Schema(description = "Notes libres", example = "Ne pas oublier le lait")
    private String notes;

    @Schema(description = "Statut de la tâche", example = "ACTIVE", allowableValues = {"ACTIVE", "ARCHIVED"})
    private String status;

    @Schema(description = "Date de complétion")
    private LocalDateTime completedAt;

    @Schema(description = "Date de création")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification")
    private LocalDateTime updatedAt;

    public TaskResponse() {
    }

    public static TaskResponse fromDomain(UserTask task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setName(task.getName());
        response.setSpoonCost(task.getSpoonCost());
        response.setImportance(task.getImportance() != null ? task.getImportance().name() : null);
        response.setCategory(task.getCategory());
        response.setDueDate(task.getDueDate());
        response.setNotes(task.getNotes());
        response.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        response.setCompletedAt(task.getCompletedAt());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
