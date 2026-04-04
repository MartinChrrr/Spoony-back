package com.spoony.backend.domain.task.service;

import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.task.model.UserTask;
import com.spoony.backend.domain.task.port.out.BaseTaskPort;
import com.spoony.backend.domain.task.port.out.TaskPort;
import com.spoony.backend.domain.shared.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskPort taskPort;

    @Mock
    private BaseTaskPort baseTaskPort;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskPort, baseTaskPort);
    }

    @Test
    void should_ReturnActiveTasks_When_FindAllActive() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserTask task1 = createTask("Courses", 3, Importance.HIGH);
        UserTask task2 = createTask("Ménage", 2, Importance.MEDIUM);
        when(taskPort.findByUserIdAndStatus(userId, TaskStatus.ACTIVE)).thenReturn(List.of(task1, task2));

        // Act
        List<UserTask> result = taskService.findAllActiveByUserId(userId);

        // Assert
        assertThat(result).hasSize(2);
        verify(taskPort).findByUserIdAndStatus(userId, TaskStatus.ACTIVE);
    }

    @Test
    void should_ReturnOverdueTasks_When_FindOverdue() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserTask overdueTask = createTask("Tâche en retard", 2, Importance.HIGH);
        overdueTask.setDueDate(LocalDate.now().minusDays(3));
        when(taskPort.findByUserIdAndDueDateBeforeAndStatus(userId, LocalDate.now(), TaskStatus.ACTIVE))
                .thenReturn(List.of(overdueTask));

        // Act
        List<UserTask> result = taskService.findOverdueByUserId(userId);

        // Assert
        assertThat(result).hasSize(1);
        verify(taskPort).findByUserIdAndDueDateBeforeAndStatus(userId, LocalDate.now(), TaskStatus.ACTIVE);
    }

    @Test
    void should_ReturnTask_When_FindByIdExists() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserTask task = createTask("Test", 2, Importance.MEDIUM);
        task.setId(taskId);
        task.setUserId(userId);
        when(taskPort.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        Optional<UserTask> result = taskService.findByIdAndUserId(taskId, userId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test");
    }

    @Test
    void should_ReturnEmpty_When_FindByIdWrongUser() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UserTask task = createTask("Test", 2, Importance.MEDIUM);
        task.setId(taskId);
        task.setUserId(ownerId);
        when(taskPort.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        Optional<UserTask> result = taskService.findByIdAndUserId(taskId, otherUserId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void should_ApplyDefaults_When_CreateWithNameOnly() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserTask task = new UserTask();
        task.setName("Nouvelle tâche");
        task.setSpoonCost(0);
        task.setImportance(null);
        task.setDueDate(null);
        when(taskPort.save(any(UserTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserTask result = taskService.create(task, userId);

        // Assert
        assertThat(result.getSpoonCost()).isEqualTo(2);
        assertThat(result.getImportance()).isEqualTo(Importance.MEDIUM);
        assertThat(result.getDueDate()).isEqualTo(LocalDate.now());
        assertThat(result.getStatus()).isEqualTo(TaskStatus.ACTIVE);
    }

    @Test
    void should_SetUserIdAndGenerateId_When_Create() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserTask task = new UserTask();
        task.setName("Test");
        when(taskPort.save(any(UserTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserTask result = taskService.create(task, userId);

        // Assert
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void should_PreserveProvidedValues_When_CreateWithAllFields() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserTask task = new UserTask();
        task.setName("Courses");
        task.setSpoonCost(4);
        task.setImportance(Importance.HIGH);
        task.setDueDate(LocalDate.of(2026, 3, 15));
        task.setCategory("ALIMENTATION");
        task.setNotes("Bio");
        when(taskPort.save(any(UserTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserTask result = taskService.create(task, userId);

        // Assert
        assertThat(result.getSpoonCost()).isEqualTo(4);
        assertThat(result.getImportance()).isEqualTo(Importance.HIGH);
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(result.getCategory()).isEqualTo("ALIMENTATION");
        assertThat(result.getNotes()).isEqualTo("Bio");
    }

    @Test
    void should_UpdateTask_When_TaskExists() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserTask existing = createTask("Ancienne tâche", 2, Importance.MEDIUM);
        existing.setId(taskId);
        existing.setUserId(userId);
        when(taskPort.findById(taskId)).thenReturn(Optional.of(existing));
        when(taskPort.save(any(UserTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserTask update = new UserTask();
        update.setName("Nouvelle tâche");
        update.setSpoonCost(4);
        update.setImportance(Importance.HIGH);

        // Act
        UserTask result = taskService.update(taskId, update, userId);

        // Assert
        assertThat(result.getName()).isEqualTo("Nouvelle tâche");
        assertThat(result.getSpoonCost()).isEqualTo(4);
        assertThat(result.getImportance()).isEqualTo(Importance.HIGH);
    }

    @Test
    void should_ThrowTaskNotFound_When_UpdateNotExists() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(taskPort.findById(taskId)).thenReturn(Optional.empty());

        UserTask update = new UserTask();
        update.setName("Update");

        // Act & Assert
        assertThatThrownBy(() -> taskService.update(taskId, update, userId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void should_ThrowTaskNotFound_When_UpdateWrongUser() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UserTask existing = createTask("Test", 2, Importance.MEDIUM);
        existing.setId(taskId);
        existing.setUserId(ownerId);
        when(taskPort.findById(taskId)).thenReturn(Optional.of(existing));

        UserTask update = new UserTask();
        update.setName("Update");

        // Act & Assert
        assertThatThrownBy(() -> taskService.update(taskId, update, otherUserId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void should_DeleteTask_When_TaskExists() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserTask existing = createTask("Test", 2, Importance.MEDIUM);
        existing.setId(taskId);
        existing.setUserId(userId);
        when(taskPort.findById(taskId)).thenReturn(Optional.of(existing));

        // Act
        taskService.delete(taskId, userId);

        // Assert
        verify(taskPort).deleteById(taskId);
    }

    @Test
    void should_ThrowTaskNotFound_When_DeleteNotExists() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(taskPort.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.delete(taskId, userId))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskPort, never()).deleteById(any());
    }

    @Test
    void should_ThrowTaskNotFound_When_DeleteWrongUser() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UserTask existing = createTask("Test", 2, Importance.MEDIUM);
        existing.setId(taskId);
        existing.setUserId(ownerId);
        when(taskPort.findById(taskId)).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> taskService.delete(taskId, otherUserId))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskPort, never()).deleteById(any());
    }

    private UserTask createTask(String name, int spoonCost, Importance importance) {
        UserTask task = new UserTask();
        task.setId(UUID.randomUUID());
        task.setName(name);
        task.setSpoonCost(spoonCost);
        task.setImportance(importance);
        task.setDueDate(LocalDate.now());
        task.setStatus(TaskStatus.ACTIVE);
        return task;
    }
}
