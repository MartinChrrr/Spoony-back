package com.spoony.backend.domain.tasklog.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserTaskLog {

    private UUID id;
    private UUID userId;
    private UUID userTaskId;
    private String taskNameSnapshot;
    private int spoonCostSnapshot;
    private LocalDate date;
    private TaskLogStatus status = TaskLogStatus.PLANNED;
    private boolean suggested = true;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;

    public UserTaskLog() {
    }

    public UserTaskLog(UUID id, UUID userId, UUID userTaskId, LocalDate date) {
        this.id = id;
        this.userId = userId;
        this.userTaskId = userTaskId;
        this.date = date;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserTaskId() {
        return userTaskId;
    }

    public void setUserTaskId(UUID userTaskId) {
        this.userTaskId = userTaskId;
    }

    public String getTaskNameSnapshot() {
        return taskNameSnapshot;
    }

    public void setTaskNameSnapshot(String taskNameSnapshot) {
        this.taskNameSnapshot = taskNameSnapshot;
    }

    public int getSpoonCostSnapshot() {
        return spoonCostSnapshot;
    }

    public void setSpoonCostSnapshot(int spoonCostSnapshot) {
        this.spoonCostSnapshot = spoonCostSnapshot;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public TaskLogStatus getStatus() {
        return status;
    }

    public void setStatus(TaskLogStatus status) {
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
