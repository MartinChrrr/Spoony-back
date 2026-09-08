package com.spoony.backend.domain.tasklog.service;

import com.spoony.backend.domain.energy.port.out.EnergyPort;
import com.spoony.backend.domain.shared.port.out.TaskPostponePort;
import com.spoony.backend.domain.tasklog.model.BulkPostponeResult;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.domain.tasklog.model.TaskSnapshot;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;
import com.spoony.backend.domain.tasklog.port.in.TaskLogUseCase;
import com.spoony.backend.domain.tasklog.port.out.TaskLogPort;
import com.spoony.backend.domain.shared.exception.EnergyNotDeclaredException;
import com.spoony.backend.domain.shared.exception.NoActiveTasksException;
import com.spoony.backend.domain.shared.exception.TaskLogExpiredException;
import com.spoony.backend.domain.shared.exception.TaskLogNotFoundException;
import com.spoony.backend.domain.shared.exception.TaskNotFoundException;
import com.spoony.backend.domain.shared.exception.SpoonBalanceConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskLogService implements TaskLogUseCase {

    private static final Logger log = LoggerFactory.getLogger(TaskLogService.class);

    private final TaskLogPort taskLogPort;
    private final EnergyPort energyPort;
    private final TaskPostponePort taskPostponePort;

    public TaskLogService(TaskLogPort taskLogPort, EnergyPort energyPort, TaskPostponePort taskPostponePort) {
        this.taskLogPort = taskLogPort;
        this.energyPort = energyPort;
        this.taskPostponePort = taskPostponePort;
    }

    @Override
    public List<UserTaskLog> getTodayLogs(UUID userId, boolean includeArchived) {
        LocalDate today = LocalDate.now();
        if (includeArchived) {
            return taskLogPort.findByUserIdAndDate(userId, today);
        }
        return taskLogPort.findByUserIdAndDateExcludeArchived(userId, today);
    }

    @Override
    public List<UserTaskLog> getLogsInRange(UUID userId, LocalDate from, LocalDate to) {
        // Calendar history: return every log in the visible range (no 24h-archive
        // filtering — past completed tasks must remain visible).
        if (from.isAfter(to)) {
            return List.of();
        }
        return taskLogPort.findByUserIdAndDateBetween(userId, from, to);
    }

    @Override
    public List<UserTaskLog> createLogs(List<UUID> userTaskIds, UUID userId) {
        LocalDate today = LocalDate.now();
        List<UserTaskLog> logs = new ArrayList<>();

        for (UUID taskId : userTaskIds) {
            // Anti-IDOR : on refuse de créer un log pour une tâche qui n'appartient
            // pas au user. TaskNotFoundException (404) plutôt que Forbidden pour ne
            // pas révéler l'existence d'une tâche d'autrui (énumération d'IDs).
            TaskSnapshot snapshot = taskLogPort.findActiveTaskSnapshotForUser(taskId, userId)
                    .orElseThrow(TaskNotFoundException::new);

            UserTaskLog taskLog = new UserTaskLog();
            taskLog.setId(UUID.randomUUID());
            taskLog.setUserId(userId);
            taskLog.setUserTaskId(taskId);
            taskLog.setTaskNameSnapshot(snapshot.name());
            taskLog.setSpoonCostSnapshot(snapshot.spoonCost());
            taskLog.setDate(today);
            taskLog.setStatus(TaskLogStatus.PLANNED);
            taskLog.setSuggested(true);
            logs.add(taskLog);
        }

        List<UserTaskLog> saved = taskLogPort.saveAll(logs);
        log.info("Task logs created userId={} count={}", userId, saved.size());
        return saved;
    }

    @Override
    public UserTaskLog createManualLog(UUID userTaskId, UUID userId) {
        LocalDate today = LocalDate.now();

        // Anti-IDOR : un user ne peut logguer que ses propres tâches.
        TaskSnapshot snapshot = taskLogPort.findActiveTaskSnapshotForUser(userTaskId, userId)
                .orElseThrow(TaskNotFoundException::new);

        UserTaskLog taskLog = new UserTaskLog();
        taskLog.setId(UUID.randomUUID());
        taskLog.setUserId(userId);
        taskLog.setUserTaskId(userTaskId);
        taskLog.setTaskNameSnapshot(snapshot.name());
        taskLog.setSpoonCostSnapshot(snapshot.spoonCost());
        taskLog.setDate(today);
        taskLog.setStatus(TaskLogStatus.PLANNED);
        taskLog.setSuggested(false);

        UserTaskLog saved = taskLogPort.save(taskLog);
        log.info("Manual task log created userId={} taskId={}", userId, userTaskId);
        return saved;
    }

    @Override
    public UserTaskLog updateStatus(UUID logId, TaskLogStatus newStatus, UUID userId) {
        UserTaskLog taskLog = taskLogPort.findById(logId)
                .orElseThrow(TaskLogNotFoundException::new);

        if (!taskLog.getUserId().equals(userId)) {
            throw new TaskLogNotFoundException();
        }

        // Règle J+1 : modification autorisée si log.date >= today - 1
        LocalDate cutoff = LocalDate.now().minusDays(1);
        if (taskLog.getDate().isBefore(cutoff)) {
            throw new TaskLogExpiredException();
        }

        TaskLogStatus oldStatus = taskLog.getStatus();
        if (oldStatus == newStatus) {
            return taskLog;
        }

        // Update status
        taskLog.setStatus(newStatus);

        // Update completedAt
        if (newStatus == TaskLogStatus.COMPLETED) {
            taskLog.setCompletedAt(LocalDateTime.now());
        }
        if (oldStatus == TaskLogStatus.COMPLETED) {
            taskLog.setCompletedAt(null);
        }

        int delta = 0;
        if (newStatus == TaskLogStatus.COMPLETED) {
            delta += taskLog.getSpoonCostSnapshot();
        }
        if (oldStatus == TaskLogStatus.COMPLETED) {
            delta -= taskLog.getSpoonCostSnapshot();
        }

        // Flush the versioned log first. A concurrent transition of this same log
        // fails here, before any spoon delta is applied. The surrounding transaction
        // also rolls this write back if the energy update fails afterwards.
        UserTaskLog saved = taskLogPort.saveAndFlush(taskLog);

        if (delta != 0 && !energyPort.adjustSpoonsUsed(userId, taskLog.getDate(), delta)) {
            if (energyPort.findByUserIdAndDate(userId, taskLog.getDate()).isEmpty()) {
                throw new EnergyNotDeclaredException();
            }
            throw new SpoonBalanceConflictException();
        }

        log.info("Task log status updated logId={} oldStatus={} newStatus={} userId={}", logId, oldStatus, newStatus, userId);
        return saved;
    }

    @Override
    public BulkPostponeResult bulkPostpone(UUID userId, LocalDate targetDate) {
        LocalDate today = LocalDate.now();
        LocalDate newDate = targetDate != null ? targetDate : today.plusDays(1);

        int count = taskPostponePort.postponeAllActiveTasks(userId, today, newDate);

        if (count == 0) {
            throw new NoActiveTasksException();
        }

        log.info("Bulk postpone userId={} count={} newDate={}", userId, count, newDate);
        return new BulkPostponeResult(count, newDate);
    }
}
