package com.spoony.backend.application.rest.tasklog;

import com.spoony.backend.domain.tasklog.model.UserTaskLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Log de tâche quotidien")
public class TaskLogResponse {

    @Schema(description = "Identifiant unique du log", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Identifiant de la tâche utilisateur", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID userTaskId;

    @Schema(description = "Date du log", example = "2026-04-05")
    private LocalDate date;

    @Schema(description = "Statut du log", example = "PLANNED", allowableValues = {"PLANNED", "COMPLETED", "SKIPPED"})
    private String status;

    @Schema(description = "Indique si la tâche a été suggérée par l'algorithme", example = "true")
    private boolean suggested;

    @Schema(description = "Date de complétion")
    private LocalDateTime completedAt;

    @Schema(description = "Date de création")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification")
    private LocalDateTime updatedAt;

    public TaskLogResponse() {
    }

    public static TaskLogResponse fromDomain(UserTaskLog log) {
        TaskLogResponse response = new TaskLogResponse();
        response.setId(log.getId());
        response.setUserTaskId(log.getUserTaskId());
        response.setDate(log.getDate());
        response.setStatus(log.getStatus().name());
        response.setSuggested(log.isSuggested());
        response.setCompletedAt(log.getCompletedAt());
        response.setCreatedAt(log.getCreatedAt());
        response.setUpdatedAt(log.getUpdatedAt());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserTaskId() {
        return userTaskId;
    }

    public void setUserTaskId(UUID userTaskId) {
        this.userTaskId = userTaskId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSuggested() {
        return suggested;
    }

    public void setSuggested(boolean suggested) {
        this.suggested = suggested;
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
