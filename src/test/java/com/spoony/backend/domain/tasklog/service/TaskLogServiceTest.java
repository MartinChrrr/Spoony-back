package com.spoony.backend.domain.tasklog.service;

import com.spoony.backend.domain.energy.port.out.EnergyPort;
import com.spoony.backend.domain.shared.port.out.TaskPostponePort;
import com.spoony.backend.domain.tasklog.model.BulkPostponeResult;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.domain.tasklog.model.TaskSnapshot;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;
import com.spoony.backend.domain.tasklog.port.out.TaskLogPort;
import com.spoony.backend.domain.shared.exception.NoActiveTasksException;
import com.spoony.backend.domain.shared.exception.TaskLogExpiredException;
import com.spoony.backend.domain.shared.exception.TaskLogNotFoundException;
import com.spoony.backend.domain.shared.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskLogServiceTest {

    @Mock
    private TaskLogPort taskLogPort;

    @Mock
    private EnergyPort energyPort;

    @Mock
    private TaskPostponePort taskPostponePort;

    private TaskLogService taskLogService;

    @BeforeEach
    void setUp() {
        taskLogService = new TaskLogService(taskLogPort, energyPort, taskPostponePort);
    }

    // --- getTodayLogs ---

    @Test
    void should_ReturnAllLogs_When_IncludeArchivedTrue() {
        // Arrange
        UUID userId = UUID.randomUUID();
        List<UserTaskLog> allLogs = List.of(createLog(userId), createLog(userId));
        when(taskLogPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(allLogs);

        // Act
        List<UserTaskLog> result = taskLogService.getTodayLogs(userId, true);

        // Assert
        assertThat(result).hasSize(2);
        verify(taskLogPort).findByUserIdAndDate(userId, LocalDate.now());
        verify(taskLogPort, never()).findByUserIdAndDateExcludeArchived(any(), any());
    }

    @Test
    void should_ReturnFilteredLogs_When_IncludeArchivedFalse() {
        // Arrange
        UUID userId = UUID.randomUUID();
        List<UserTaskLog> filteredLogs = List.of(createLog(userId));
        when(taskLogPort.findByUserIdAndDateExcludeArchived(userId, LocalDate.now())).thenReturn(filteredLogs);

        // Act
        List<UserTaskLog> result = taskLogService.getTodayLogs(userId, false);

        // Assert
        assertThat(result).hasSize(1);
        verify(taskLogPort).findByUserIdAndDateExcludeArchived(userId, LocalDate.now());
        verify(taskLogPort, never()).findByUserIdAndDate(any(), any());
    }

    // --- getLogsInRange ---

    @Test
    void should_ReturnLogsInRange_When_RangeValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2026, 4, 30);
        List<UserTaskLog> rangeLogs = List.of(createLog(userId), createLog(userId), createLog(userId));
        when(taskLogPort.findByUserIdAndDateBetween(userId, from, to)).thenReturn(rangeLogs);

        // Act
        List<UserTaskLog> result = taskLogService.getLogsInRange(userId, from, to);

        // Assert
        assertThat(result).hasSize(3);
        verify(taskLogPort).findByUserIdAndDateBetween(userId, from, to);
    }

    @Test
    void should_ReturnEmpty_When_FromAfterTo() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2026, 4, 30);
        LocalDate to = LocalDate.of(2026, 4, 1);

        // Act
        List<UserTaskLog> result = taskLogService.getLogsInRange(userId, from, to);

        // Assert
        assertThat(result).isEmpty();
        verify(taskLogPort, never()).findByUserIdAndDateBetween(any(), any(), any());
    }

    // --- createLogs ---

    @Test
    void should_CreatePlannedLogs_When_ValidTaskIds() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId1 = UUID.randomUUID();
        UUID taskId2 = UUID.randomUUID();
        when(taskLogPort.findActiveTaskSnapshotForUser(taskId1, userId))
                .thenReturn(Optional.of(new TaskSnapshot("Task 1", 2)));
        when(taskLogPort.findActiveTaskSnapshotForUser(taskId2, userId))
                .thenReturn(Optional.of(new TaskSnapshot("Task 2", 4)));
        when(taskLogPort.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<UserTaskLog> result = taskLogService.createLogs(List.of(taskId1, taskId2), userId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(log -> {
            assertThat(log.getUserId()).isEqualTo(userId);
            assertThat(log.getStatus()).isEqualTo(TaskLogStatus.PLANNED);
            assertThat(log.getDate()).isEqualTo(LocalDate.now());
            assertThat(log.getId()).isNotNull();
            assertThat(log.getTaskNameSnapshot()).isNotBlank();
        });
        assertThat(result).extracting(UserTaskLog::getSpoonCostSnapshot).containsExactly(2, 4);
    }

    @Test
    void should_SetSuggestedTrue_When_BulkCreate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        when(taskLogPort.findActiveTaskSnapshotForUser(taskId, userId))
                .thenReturn(Optional.of(new TaskSnapshot("Task", 2)));
        when(taskLogPort.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<UserTaskLog> result = taskLogService.createLogs(List.of(taskId), userId);

        // Assert
        assertThat(result.get(0).isSuggested()).isTrue();
    }

    // --- createManualLog ---

    @Test
    void should_CreateLog_When_ValidTaskId() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        when(taskLogPort.findActiveTaskSnapshotForUser(taskId, userId))
                .thenReturn(Optional.of(new TaskSnapshot("Historical name", 3)));
        when(taskLogPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserTaskLog result = taskLogService.createManualLog(taskId, userId);

        // Assert
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUserTaskId()).isEqualTo(taskId);
        assertThat(result.getStatus()).isEqualTo(TaskLogStatus.PLANNED);
        assertThat(result.getDate()).isEqualTo(LocalDate.now());
        assertThat(result.getTaskNameSnapshot()).isEqualTo("Historical name");
        assertThat(result.getSpoonCostSnapshot()).isEqualTo(3);
    }

    @Test
    void should_SetSuggestedFalse_When_ManualCreate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        when(taskLogPort.findActiveTaskSnapshotForUser(taskId, userId))
                .thenReturn(Optional.of(new TaskSnapshot("Task", 2)));
        when(taskLogPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserTaskLog result = taskLogService.createManualLog(taskId, userId);

        // Assert
        assertThat(result.isSuggested()).isFalse();
    }

    @Test
    void should_ThrowTaskNotFound_When_ManualLogOnOtherUsersTask() {
        // Arrange — IDOR : la tâche n'appartient pas au user → rejet, aucune insertion.
        UUID userId = UUID.randomUUID();
        UUID otherUsersTaskId = UUID.randomUUID();
        when(taskLogPort.findActiveTaskSnapshotForUser(otherUsersTaskId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskLogService.createManualLog(otherUsersTaskId, userId))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskLogPort, never()).save(any());
    }

    @Test
    void should_ThrowTaskNotFound_When_BulkContainsOtherUsersTask() {
        // Arrange — un seul ID étranger dans le lot suffit à tout rejeter.
        UUID userId = UUID.randomUUID();
        UUID ownTaskId = UUID.randomUUID();
        UUID otherUsersTaskId = UUID.randomUUID();
        when(taskLogPort.findActiveTaskSnapshotForUser(ownTaskId, userId))
                .thenReturn(Optional.of(new TaskSnapshot("Own task", 2)));
        when(taskLogPort.findActiveTaskSnapshotForUser(otherUsersTaskId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskLogService.createLogs(List.of(ownTaskId, otherUsersTaskId), userId))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskLogPort, never()).saveAll(any());
    }

    // --- updateStatus ---

    @Test
    void should_CompleteAndIncrementSpoons_When_StatusToCompleted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UserTaskLog log = createLog(userId);
        log.setId(logId);
        log.setStatus(TaskLogStatus.PLANNED);
        log.setDate(LocalDate.now());
        log.setSpoonCostSnapshot(3);

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));
        when(taskLogPort.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(energyPort.adjustSpoonsUsed(userId, LocalDate.now(), 3)).thenReturn(true);

        // Act
        UserTaskLog result = taskLogService.updateStatus(logId, TaskLogStatus.COMPLETED, userId);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TaskLogStatus.COMPLETED);
        verify(energyPort).adjustSpoonsUsed(userId, LocalDate.now(), 3);
    }

    @Test
    void should_UncompleteAndDecrementSpoons_When_StatusFromCompleted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UserTaskLog log = createLog(userId);
        log.setId(logId);
        log.setStatus(TaskLogStatus.COMPLETED);
        log.setCompletedAt(LocalDateTime.now());
        log.setDate(LocalDate.now());
        log.setSpoonCostSnapshot(3);

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));
        when(taskLogPort.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(energyPort.adjustSpoonsUsed(userId, LocalDate.now(), -3)).thenReturn(true);

        // Act
        UserTaskLog result = taskLogService.updateStatus(logId, TaskLogStatus.SKIPPED, userId);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TaskLogStatus.SKIPPED);
        verify(energyPort).adjustSpoonsUsed(userId, LocalDate.now(), -3);
    }

    @Test
    void should_SetCompletedAt_When_StatusToCompleted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UserTaskLog log = createLog(userId);
        log.setId(logId);
        log.setStatus(TaskLogStatus.PLANNED);
        log.setDate(LocalDate.now());

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));
        when(taskLogPort.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(energyPort.adjustSpoonsUsed(userId, LocalDate.now(), 2)).thenReturn(true);

        // Act
        UserTaskLog result = taskLogService.updateStatus(logId, TaskLogStatus.COMPLETED, userId);

        // Assert
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void should_ClearCompletedAt_When_StatusFromCompleted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UserTaskLog log = createLog(userId);
        log.setId(logId);
        log.setStatus(TaskLogStatus.COMPLETED);
        log.setCompletedAt(LocalDateTime.now());
        log.setDate(LocalDate.now());

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));
        when(taskLogPort.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(energyPort.adjustSpoonsUsed(userId, LocalDate.now(), -2)).thenReturn(true);

        // Act
        UserTaskLog result = taskLogService.updateStatus(logId, TaskLogStatus.PLANNED, userId);

        // Assert
        assertThat(result.getCompletedAt()).isNull();
    }

    @Test
    void should_ThrowNotFound_When_LogNotExists() {
        // Arrange
        UUID logId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(taskLogPort.findById(logId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskLogService.updateStatus(logId, TaskLogStatus.COMPLETED, userId))
                .isInstanceOf(TaskLogNotFoundException.class);
    }

    @Test
    void should_ThrowNotFound_When_WrongUser() {
        // Arrange
        UUID logId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UserTaskLog log = createLog(ownerId);
        log.setId(logId);
        log.setDate(LocalDate.now());
        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));

        // Act & Assert
        assertThatThrownBy(() -> taskLogService.updateStatus(logId, TaskLogStatus.COMPLETED, otherUserId))
                .isInstanceOf(TaskLogNotFoundException.class);
    }

    @Test
    void should_ThrowExpired_When_LogOlderThanJ1() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UserTaskLog log = createLog(userId);
        log.setId(logId);
        log.setDate(LocalDate.now().minusDays(2)); // J-2 = trop vieux

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));

        // Act & Assert
        assertThatThrownBy(() -> taskLogService.updateStatus(logId, TaskLogStatus.COMPLETED, userId))
                .isInstanceOf(TaskLogExpiredException.class);
    }

    @Test
    void should_NotThrowExpired_When_LogIsYesterday() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UserTaskLog log = createLog(userId);
        log.setId(logId);
        log.setStatus(TaskLogStatus.PLANNED);
        log.setDate(LocalDate.now().minusDays(1)); // J-1 = OK

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));
        when(taskLogPort.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(energyPort.adjustSpoonsUsed(userId, log.getDate(), 2)).thenReturn(true);

        // Act
        UserTaskLog result = taskLogService.updateStatus(logId, TaskLogStatus.COMPLETED, userId);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TaskLogStatus.COMPLETED);
    }

    @Test
    void should_NoOp_When_SameStatus() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        UserTaskLog log = createLog(userId);
        log.setId(logId);
        log.setStatus(TaskLogStatus.PLANNED);
        log.setDate(LocalDate.now());

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));

        // Act
        UserTaskLog result = taskLogService.updateStatus(logId, TaskLogStatus.PLANNED, userId);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TaskLogStatus.PLANNED);
        verify(taskLogPort, never()).saveAndFlush(any());
        verify(energyPort, never()).adjustSpoonsUsed(any(), any(), anyInt());
    }

    // --- bulkPostpone ---

    @Test
    void should_PostponeActiveTasks_When_TasksExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        when(taskPostponePort.postponeAllActiveTasks(userId, today, tomorrow)).thenReturn(2);

        // Act
        BulkPostponeResult result = taskLogService.bulkPostpone(userId, null);

        // Assert
        assertThat(result.getPostponedCount()).isEqualTo(2);
        assertThat(result.getNewDate()).isEqualTo(tomorrow);
        verify(taskPostponePort).postponeAllActiveTasks(userId, today, tomorrow);
    }

    @Test
    void should_ThrowNoActiveTasks_When_NoActiveTasks() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        when(taskPostponePort.postponeAllActiveTasks(userId, today, tomorrow)).thenReturn(0);

        // Act & Assert
        assertThatThrownBy(() -> taskLogService.bulkPostpone(userId, null))
                .isInstanceOf(NoActiveTasksException.class);
    }

    @Test
    void should_UseTargetDate_When_Provided() {
        // Arrange
        UUID userId = UUID.randomUUID();
        LocalDate targetDate = LocalDate.of(2026, 3, 20);
        when(taskPostponePort.postponeAllActiveTasks(userId, LocalDate.now(), targetDate)).thenReturn(1);

        // Act
        BulkPostponeResult result = taskLogService.bulkPostpone(userId, targetDate);

        // Assert
        assertThat(result.getPostponedCount()).isEqualTo(1);
        assertThat(result.getNewDate()).isEqualTo(targetDate);
        verify(taskPostponePort).postponeAllActiveTasks(userId, LocalDate.now(), targetDate);
    }

    @Test
    void should_DelegateToPostponePort_When_BulkPostpone() {
        // Arrange — la suppression des logs PLANNED est faite dans TaskPostponeAdapter (infra),
        // pas dans le service domaine. Le service délègue simplement au port.
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        when(taskPostponePort.postponeAllActiveTasks(userId, today, tomorrow)).thenReturn(2);

        // Act
        BulkPostponeResult result = taskLogService.bulkPostpone(userId, null);

        // Assert
        assertThat(result.getPostponedCount()).isEqualTo(2);
        verify(taskPostponePort).postponeAllActiveTasks(userId, today, tomorrow);
    }

    // --- helpers ---

    private UserTaskLog createLog(UUID userId) {
        UserTaskLog log = new UserTaskLog();
        log.setId(UUID.randomUUID());
        log.setUserId(userId);
        log.setUserTaskId(UUID.randomUUID());
        log.setTaskNameSnapshot("Task");
        log.setSpoonCostSnapshot(2);
        log.setDate(LocalDate.now());
        log.setStatus(TaskLogStatus.PLANNED);
        log.setSuggested(true);
        return log;
    }

}
