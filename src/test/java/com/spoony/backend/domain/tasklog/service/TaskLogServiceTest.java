package com.spoony.backend.domain.tasklog.service;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.domain.energy.port.out.EnergyPort;
import com.spoony.backend.domain.shared.port.out.TaskPostponePort;
import com.spoony.backend.domain.tasklog.model.BulkPostponeResult;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;
import com.spoony.backend.domain.tasklog.port.out.TaskLogPort;
import com.spoony.backend.domain.shared.exception.NoActiveTasksException;
import com.spoony.backend.domain.shared.exception.TaskLogExpiredException;
import com.spoony.backend.domain.shared.exception.TaskLogNotFoundException;
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

    // --- createLogs ---

    @Test
    void should_CreatePlannedLogs_When_ValidTaskIds() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId1 = UUID.randomUUID();
        UUID taskId2 = UUID.randomUUID();
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
        });
    }

    @Test
    void should_SetSuggestedTrue_When_BulkCreate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
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
        when(taskLogPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserTaskLog result = taskLogService.createManualLog(taskId, userId);

        // Assert
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUserTaskId()).isEqualTo(taskId);
        assertThat(result.getStatus()).isEqualTo(TaskLogStatus.PLANNED);
        assertThat(result.getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void should_SetSuggestedFalse_When_ManualCreate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        when(taskLogPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserTaskLog result = taskLogService.createManualLog(taskId, userId);

        // Assert
        assertThat(result.isSuggested()).isFalse();
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

        DailyEnergy energy = createEnergy(userId, 8, 2);

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));
        when(taskLogPort.findSpoonCostByTaskId(log.getUserTaskId())).thenReturn(3);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.of(energy));
        when(taskLogPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(energyPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserTaskLog result = taskLogService.updateStatus(logId, TaskLogStatus.COMPLETED, userId);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TaskLogStatus.COMPLETED);
        assertThat(energy.getSpoonsUsed()).isEqualTo(5); // 2 + 3
        verify(energyPort).save(energy);
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

        DailyEnergy energy = createEnergy(userId, 8, 5);

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));
        when(taskLogPort.findSpoonCostByTaskId(log.getUserTaskId())).thenReturn(3);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.of(energy));
        when(taskLogPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(energyPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserTaskLog result = taskLogService.updateStatus(logId, TaskLogStatus.SKIPPED, userId);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TaskLogStatus.SKIPPED);
        assertThat(energy.getSpoonsUsed()).isEqualTo(2); // 5 - 3
        verify(energyPort).save(energy);
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

        DailyEnergy energy = createEnergy(userId, 8, 0);

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));
        when(taskLogPort.findSpoonCostByTaskId(log.getUserTaskId())).thenReturn(2);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.of(energy));
        when(taskLogPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(energyPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

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

        DailyEnergy energy = createEnergy(userId, 8, 3);

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));
        when(taskLogPort.findSpoonCostByTaskId(log.getUserTaskId())).thenReturn(2);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.of(energy));
        when(taskLogPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(energyPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

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

        DailyEnergy energy = createEnergy(userId, 8, 0);

        when(taskLogPort.findById(logId)).thenReturn(Optional.of(log));
        when(taskLogPort.findSpoonCostByTaskId(log.getUserTaskId())).thenReturn(2);
        when(energyPort.findByUserIdAndDate(userId, log.getDate())).thenReturn(Optional.of(energy));
        when(taskLogPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(energyPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

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
        verify(taskLogPort, never()).save(any());
        verify(energyPort, never()).save(any());
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
    void should_NotTouchTaskLogs_When_BulkPostpone() {
        // Arrange — la sémantique du bulk postpone porte sur user_tasks, pas sur user_task_logs.
        // Les logs PLANNED éventuels du jour ne sont PAS modifiés.
        UUID userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        when(taskPostponePort.postponeAllActiveTasks(userId, today, tomorrow)).thenReturn(2);

        // Act
        taskLogService.bulkPostpone(userId, null);

        // Assert
        verify(taskLogPort, never()).findByUserIdAndDateAndStatus(any(), any(), any());
        verify(taskLogPort, never()).saveAll(any());
    }

    // --- helpers ---

    private UserTaskLog createLog(UUID userId) {
        UserTaskLog log = new UserTaskLog();
        log.setId(UUID.randomUUID());
        log.setUserId(userId);
        log.setUserTaskId(UUID.randomUUID());
        log.setDate(LocalDate.now());
        log.setStatus(TaskLogStatus.PLANNED);
        log.setSuggested(true);
        return log;
    }

    private DailyEnergy createEnergy(UUID userId, int spoons, int spoonsUsed) {
        DailyEnergy energy = new DailyEnergy();
        energy.setId(UUID.randomUUID());
        energy.setUserId(userId);
        energy.setDate(LocalDate.now());
        energy.setSpoons(spoons);
        energy.setSpoonsUsed(spoonsUsed);
        return energy;
    }
}
