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
        log.setDate(entity.getDate());
        log.setStatus(entity.getStatus());
        log.setCompletedAt(entity.getCompletedAt());
        log.setCreatedAt(entity.getCreatedAt());
        log.setUpdatedAt(entity.getUpdatedAt());
        return log;
    }

    public static UserTaskLogEntity toEntity(UserTaskLog log) {
        UserTaskLogEntity entity = new UserTaskLogEntity();
        entity.setId(log.getId());
        entity.setUserId(log.getUserId());
        entity.setUserTaskId(log.getUserTaskId());
        entity.setDate(log.getDate());
        entity.setStatus(log.getStatus());
        entity.setCompletedAt(log.getCompletedAt());
        return entity;
    }
}
