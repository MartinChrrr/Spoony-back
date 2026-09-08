package com.spoony.backend.infrastructure.persistence.mapper;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskLogMapperTest {

    @Test
    void should_ConvertEntityToDomain_When_AllFieldsPresent() {
        // Arrange
        UserTaskLogEntity entity = new UserTaskLogEntity();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID userTaskId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setUserTaskId(userTaskId);
        entity.setTaskNameSnapshot("Historical task");
        entity.setSpoonCostSnapshot((short) 4);
        entity.setVersion(7);
        entity.setDate(LocalDate.of(2026, 3, 1));
        entity.setStatus(TaskLogStatus.COMPLETED);
        entity.setCompletedAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // Act
        UserTaskLog log = TaskLogMapper.toDomain(entity);

        // Assert
        assertThat(log.getId()).isEqualTo(id);
        assertThat(log.getUserId()).isEqualTo(userId);
        assertThat(log.getUserTaskId()).isEqualTo(userTaskId);
        assertThat(log.getTaskNameSnapshot()).isEqualTo("Historical task");
        assertThat(log.getSpoonCostSnapshot()).isEqualTo(4);
        assertThat(log.getVersion()).isEqualTo(7);
        assertThat(log.getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(log.getStatus()).isEqualTo(TaskLogStatus.COMPLETED);
        assertThat(log.getCompletedAt()).isEqualTo(now);
        assertThat(log.getCreatedAt()).isEqualTo(now);
        assertThat(log.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void should_ConvertDomainToEntity_When_AllFieldsPresent() {
        // Arrange
        UserTaskLog log = new UserTaskLog();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID userTaskId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        log.setId(id);
        log.setUserId(userId);
        log.setUserTaskId(userTaskId);
        log.setTaskNameSnapshot("Historical task");
        log.setSpoonCostSnapshot(4);
        log.setVersion(7);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        log.setDate(LocalDate.of(2026, 3, 1));
        log.setStatus(TaskLogStatus.SKIPPED);
        log.setCompletedAt(now);

        // Act
        UserTaskLogEntity entity = TaskLogMapper.toEntity(log);

        // Assert
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getUserTaskId()).isEqualTo(userTaskId);
        assertThat(entity.getTaskNameSnapshot()).isEqualTo("Historical task");
        assertThat(entity.getSpoonCostSnapshot()).isEqualTo((short) 4);
        assertThat(entity.getVersion()).isEqualTo(7);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(entity.getStatus()).isEqualTo(TaskLogStatus.SKIPPED);
        assertThat(entity.getCompletedAt()).isEqualTo(now);
    }

    @Test
    void should_DefaultToPlanned_When_EntityHasDefaultStatus() {
        // Arrange
        UserTaskLogEntity entity = new UserTaskLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setUserTaskId(UUID.randomUUID());
        entity.setDate(LocalDate.now());

        // Act
        UserTaskLog log = TaskLogMapper.toDomain(entity);

        // Assert
        assertThat(log.getStatus()).isEqualTo(TaskLogStatus.PLANNED);
    }

    @Test
    void should_HandleNullCompletedAt_When_TaskNotCompleted() {
        // Arrange
        UserTaskLogEntity entity = new UserTaskLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setUserTaskId(UUID.randomUUID());
        entity.setDate(LocalDate.now());
        entity.setStatus(TaskLogStatus.PLANNED);
        entity.setCompletedAt(null);

        // Act
        UserTaskLog log = TaskLogMapper.toDomain(entity);

        // Assert
        assertThat(log.getCompletedAt()).isNull();
    }
}
