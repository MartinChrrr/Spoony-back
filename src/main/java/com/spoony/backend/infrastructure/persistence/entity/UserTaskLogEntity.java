package com.spoony.backend.infrastructure.persistence.entity;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_task_logs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_task_id", "date"})
})
public class UserTaskLogEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_task_id", nullable = false)
    private UUID userTaskId;

    @Column(name = "task_name_snapshot", nullable = false, length = 255)
    private String taskNameSnapshot;

    @Column(name = "spoon_cost_snapshot", nullable = false)
    private short spoonCostSnapshot;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskLogStatus status = TaskLogStatus.PLANNED;

    @Column(name = "suggested", nullable = false)
    private boolean suggested = true;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public UserTaskLogEntity() {
    }

    public UserTaskLogEntity(UUID userId, UUID userTaskId, LocalDate date) {
        this.userId = userId;
        this.userTaskId = userTaskId;
        this.date = date;
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

    public short getSpoonCostSnapshot() {
        return spoonCostSnapshot;
    }

    public void setSpoonCostSnapshot(short spoonCostSnapshot) {
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
