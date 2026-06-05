package com.spoony.backend.infrastructure.persistence.adapter;

import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskLogRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskPostponeAdapterTest {

    @Mock
    private JpaUserTaskRepository userTaskRepository;

    @Mock
    private JpaUserTaskLogRepository userTaskLogRepository;

    private TaskPostponeAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TaskPostponeAdapter(userTaskRepository, userTaskLogRepository);
    }

    @Test
    void should_PostponeActiveTasks_When_TasksDueOnFromDate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        UserTaskEntity task1 = createTaskEntity(userId, today);
        UserTaskEntity task2 = createTaskEntity(userId, today);
        when(userTaskRepository.findByUserIdAndStatusAndDueDate(userId, TaskStatus.ACTIVE, today))
                .thenReturn(List.of(task1, task2));
        when(userTaskLogRepository.findByUserIdAndDateAndStatus(userId, today, TaskLogStatus.PLANNED))
                .thenReturn(List.of());

        // Act
        int count = adapter.postponeAllActiveTasks(userId, today, tomorrow);

        // Assert
        assertThat(count).isEqualTo(2);
        assertThat(task1.getDueDate()).isEqualTo(tomorrow);
        assertThat(task2.getDueDate()).isEqualTo(tomorrow);
        verify(userTaskRepository).saveAll(List.of(task1, task2));
    }

    @Test
    void should_DeletePlannedLogs_When_PlannedLogsExistOnFromDate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        when(userTaskRepository.findByUserIdAndStatusAndDueDate(userId, TaskStatus.ACTIVE, today))
                .thenReturn(List.of());

        UserTaskLogEntity plannedLog1 = createLogEntity(userId, today, TaskLogStatus.PLANNED);
        UserTaskLogEntity plannedLog2 = createLogEntity(userId, today, TaskLogStatus.PLANNED);
        when(userTaskLogRepository.findByUserIdAndDateAndStatus(userId, today, TaskLogStatus.PLANNED))
                .thenReturn(List.of(plannedLog1, plannedLog2));

        // Act
        adapter.postponeAllActiveTasks(userId, today, tomorrow);

        // Assert
        verify(userTaskLogRepository).deleteAll(List.of(plannedLog1, plannedLog2));
    }

    @Test
    void should_NotDeleteCompletedOrSkippedLogs_When_PostponingTasks() {
        // Arrange — seuls les PLANNED sont supprimés ; COMPLETED et SKIPPED ne sont pas touchés.
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        when(userTaskRepository.findByUserIdAndStatusAndDueDate(userId, TaskStatus.ACTIVE, today))
                .thenReturn(List.of());
        // Le repository ne retourne que des PLANNED (filtre SQL) — donc seuls ceux-là sont supprimés.
        when(userTaskLogRepository.findByUserIdAndDateAndStatus(userId, today, TaskLogStatus.PLANNED))
                .thenReturn(List.of());

        // Act
        adapter.postponeAllActiveTasks(userId, today, tomorrow);

        // Assert — deleteAll est appelé avec une liste vide (aucun PLANNED) ; les autres logs intacts.
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserTaskLogEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(userTaskLogRepository).deleteAll(captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    void should_ReturnZero_When_NoActiveTasks() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        when(userTaskRepository.findByUserIdAndStatusAndDueDate(userId, TaskStatus.ACTIVE, today))
                .thenReturn(List.of());
        when(userTaskLogRepository.findByUserIdAndDateAndStatus(userId, today, TaskLogStatus.PLANNED))
                .thenReturn(List.of());

        // Act
        int count = adapter.postponeAllActiveTasks(userId, today, tomorrow);

        // Assert
        assertThat(count).isEqualTo(0);
    }

    @Test
    void should_SaveTasksAndDeleteLogs_WhenBothExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        UserTaskEntity task = createTaskEntity(userId, today);
        when(userTaskRepository.findByUserIdAndStatusAndDueDate(userId, TaskStatus.ACTIVE, today))
                .thenReturn(List.of(task));

        UserTaskLogEntity log = createLogEntity(userId, today, TaskLogStatus.PLANNED);
        when(userTaskLogRepository.findByUserIdAndDateAndStatus(userId, today, TaskLogStatus.PLANNED))
                .thenReturn(List.of(log));

        // Act
        int count = adapter.postponeAllActiveTasks(userId, today, tomorrow);

        // Assert
        assertThat(count).isEqualTo(1);
        assertThat(task.getDueDate()).isEqualTo(tomorrow);
        verify(userTaskRepository).saveAll(any());
        verify(userTaskLogRepository).deleteAll(List.of(log));
    }

    // --- helpers ---

    private UserTaskEntity createTaskEntity(UUID userId, LocalDate dueDate) {
        UserTaskEntity entity = new UserTaskEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setDueDate(dueDate);
        entity.setStatus(TaskStatus.ACTIVE);
        return entity;
    }

    private UserTaskLogEntity createLogEntity(UUID userId, LocalDate date, TaskLogStatus status) {
        UserTaskLogEntity entity = new UserTaskLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setUserTaskId(UUID.randomUUID());
        entity.setDate(date);
        entity.setStatus(status);
        return entity;
    }
}
