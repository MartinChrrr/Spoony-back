package com.spoony.backend.infrastructure.persistence.mapper;

import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.task.model.UserTask;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;

import java.time.LocalDate;

public final class TaskMapper {

    private TaskMapper() {
    }

    public static UserTask toDomain(UserTaskEntity entity) {
        UserTask task = new UserTask();
        task.setId(entity.getId());
        task.setUserId(entity.getUserId());
        task.setName(entity.getName());
        task.setSpoonCost(entity.getSpoonCost());
        task.setImportance(Importance.valueOf(entity.getImportance()));
        task.setCategory(entity.getCategory());
        task.setDueDate(entity.getDueDate());
        task.setNotes(entity.getNotes());
        task.setStatus(entity.getStatus());
        task.setCompletedAt(entity.getCompletedAt());
        task.setCreatedAt(entity.getCreatedAt());
        task.setUpdatedAt(entity.getUpdatedAt());
        return task;
    }

    public static UserTaskEntity toEntity(UserTask task) {
        UserTaskEntity entity = new UserTaskEntity();
        entity.setId(task.getId());
        entity.setUserId(task.getUserId());
        entity.setName(task.getName());
        entity.setSpoonCost((short) (task.getSpoonCost() > 0 ? task.getSpoonCost() : 2));
        entity.setImportance(task.getImportance() != null ? task.getImportance().name() : "MEDIUM");
        entity.setCategory(task.getCategory());
        entity.setDueDate(task.getDueDate() != null ? task.getDueDate() : LocalDate.now());
        entity.setNotes(task.getNotes());
        entity.setStatus(task.getStatus() != null ? task.getStatus() : TaskStatus.ACTIVE);
        entity.setCompletedAt(task.getCompletedAt());
        return entity;
    }
}
