package com.spoony.backend.infrastructure.persistence.mapper;

import com.spoony.backend.domain.tasklog.model.UserTaskLog;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;

public final class TaskLogMapper {

    private TaskLogMapper() {
    }

    public static UserTaskLog toDomain(UserTaskLogEntity entity) {
        UserTaskLog log = new UserTaskLog();
        log.setId(entity.getId());
        log.setUserId(entity.getUserId());
        log.setUserTaskId(entity.getUserTaskId());
        log.setTaskNameSnapshot(entity.getTaskNameSnapshot());
        log.setSpoonCostSnapshot(entity.getSpoonCostSnapshot());
        log.setDate(entity.getDate());
        log.setStatus(entity.getStatus());
        log.setSuggested(entity.isSuggested());
        log.setCompletedAt(entity.getCompletedAt());
        log.setCreatedAt(entity.getCreatedAt());
        log.setUpdatedAt(entity.getUpdatedAt());
        log.setVersion(entity.getVersion());
        return log;
    }

    public static UserTaskLogEntity toEntity(UserTaskLog log) {
        UserTaskLogEntity entity = new UserTaskLogEntity();
        entity.setId(log.getId());
        entity.setUserId(log.getUserId());
        entity.setUserTaskId(log.getUserTaskId());
        entity.setTaskNameSnapshot(log.getTaskNameSnapshot());
        entity.setSpoonCostSnapshot((short) log.getSpoonCostSnapshot());
        entity.setDate(log.getDate());
        entity.setStatus(log.getStatus());
        entity.setSuggested(log.isSuggested());
        entity.setCompletedAt(log.getCompletedAt());
        entity.setVersion(log.getVersion());
        if (log.getCreatedAt() != null) {
            entity.setCreatedAt(log.getCreatedAt());
        }
        if (log.getUpdatedAt() != null) {
            entity.setUpdatedAt(log.getUpdatedAt());
        }
        return entity;
    }
}
