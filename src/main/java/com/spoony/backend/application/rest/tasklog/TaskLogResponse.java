package com.spoony.backend.application.rest.tasklog;

import com.spoony.backend.domain.tasklog.model.UserTaskLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TaskLogResponse {

    private UUID id;
    private UUID userTaskId;
    private LocalDate date;
    private String status;
    private boolean suggested;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
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
