package com.spoony.backend.infrastructure.persistence.mapper;

import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.task.model.UserTask;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

    @Test
    void should_ConvertEntityToDomain_When_AllFieldsPresent() {
        // Arrange
        UserTaskEntity entity = new UserTaskEntity();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setName("Faire les courses");
        entity.setSpoonCost((short) 3);
        entity.setImportance("HIGH");
        entity.setCategory("ALIMENTATION");
        entity.setDueDate(LocalDate.of(2026, 3, 1));
        entity.setNotes("Bio de préférence");
        entity.setStatus(TaskStatus.ACTIVE);
        entity.setCompletedAt(null);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // Act
        UserTask task = TaskMapper.toDomain(entity);

        // Assert
        assertThat(task.getId()).isEqualTo(id);
        assertThat(task.getUserId()).isEqualTo(userId);
        assertThat(task.getName()).isEqualTo("Faire les courses");
        assertThat(task.getSpoonCost()).isEqualTo(3);
        assertThat(task.getImportance()).isEqualTo(Importance.HIGH);
        assertThat(task.getCategory()).isEqualTo("ALIMENTATION");
        assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(task.getNotes()).isEqualTo("Bio de préférence");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.ACTIVE);
        assertThat(task.getCompletedAt()).isNull();
        assertThat(task.getCreatedAt()).isEqualTo(now);
        assertThat(task.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void should_ConvertDomainToEntity_When_AllFieldsPresent() {
        // Arrange
        UserTask task = new UserTask();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        task.setId(id);
        task.setUserId(userId);
        task.setName("Faire les courses");
        task.setSpoonCost(3);
        task.setImportance(Importance.HIGH);
        task.setCategory("ALIMENTATION");
        task.setDueDate(LocalDate.of(2026, 3, 1));
        task.setNotes("Bio de préférence");
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        // Act
        UserTaskEntity entity = TaskMapper.toEntity(task);

        // Assert
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getName()).isEqualTo("Faire les courses");
        assertThat(entity.getSpoonCost()).isEqualTo((short) 3);
        assertThat(entity.getImportance()).isEqualTo("HIGH");
        assertThat(entity.getCategory()).isEqualTo("ALIMENTATION");
        assertThat(entity.getDueDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(entity.getNotes()).isEqualTo("Bio de préférence");
        assertThat(entity.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(entity.getCompletedAt()).isNotNull();
    }

    @Test
    void should_ApplyDefaultSpoonCost_When_SpoonCostIsZero() {
        // Arrange
        UserTask task = new UserTask();
        task.setId(UUID.randomUUID());
        task.setUserId(UUID.randomUUID());
        task.setName("Test");
        task.setSpoonCost(0);

        // Act
        UserTaskEntity entity = TaskMapper.toEntity(task);

        // Assert
        assertThat(entity.getSpoonCost()).isEqualTo((short) 2);
    }

    @Test
    void should_ApplyDefaultImportance_When_ImportanceIsNull() {
        // Arrange
        UserTask task = new UserTask();
        task.setId(UUID.randomUUID());
        task.setUserId(UUID.randomUUID());
        task.setName("Test");
        task.setImportance(null);

        // Act
        UserTaskEntity entity = TaskMapper.toEntity(task);

        // Assert
        assertThat(entity.getImportance()).isEqualTo("MEDIUM");
    }

    @Test
    void should_ApplyDefaultDueDate_When_DueDateIsNull() {
        // Arrange
        UserTask task = new UserTask();
        task.setId(UUID.randomUUID());
        task.setUserId(UUID.randomUUID());
        task.setName("Test");
        task.setDueDate(null);

        // Act
        UserTaskEntity entity = TaskMapper.toEntity(task);

        // Assert
        assertThat(entity.getDueDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void should_ApplyDefaultStatus_When_StatusIsNull() {
        // Arrange
        UserTask task = new UserTask();
        task.setId(UUID.randomUUID());
        task.setUserId(UUID.randomUUID());
        task.setName("Test");
        task.setStatus(null);

        // Act
        UserTaskEntity entity = TaskMapper.toEntity(task);

        // Assert
        assertThat(entity.getStatus()).isEqualTo(TaskStatus.ACTIVE);
    }

    @Test
    void should_HandleNullableFields_When_CategoryAndNotesAreNull() {
        // Arrange
        UserTaskEntity entity = new UserTaskEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setName("Test");
        entity.setSpoonCost((short) 2);
        entity.setImportance("MEDIUM");
        entity.setDueDate(LocalDate.now());
        entity.setStatus(TaskStatus.ACTIVE);
        entity.setCategory(null);
        entity.setNotes(null);

        // Act
        UserTask task = TaskMapper.toDomain(entity);

        // Assert
        assertThat(task.getCategory()).isNull();
        assertThat(task.getNotes()).isNull();
    }
}
