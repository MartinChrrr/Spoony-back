package com.spoony.backend.application.tasklog;

import com.spoony.backend.domain.tasklog.model.BulkPostponeResult;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;
import com.spoony.backend.domain.tasklog.port.in.TaskLogUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TaskLogApplicationService {

    private final TaskLogUseCase taskLogUseCase;

    public TaskLogApplicationService(TaskLogUseCase taskLogUseCase) {
        this.taskLogUseCase = taskLogUseCase;
    }

    public List<UserTaskLog> getTodayLogs(UUID userId, boolean includeArchived) {
        return taskLogUseCase.getTodayLogs(userId, includeArchived);
    }

    public List<UserTaskLog> getLogsInRange(UUID userId, LocalDate from, LocalDate to) {
        return taskLogUseCase.getLogsInRange(userId, from, to);
    }

    @Transactional
    public List<UserTaskLog> createLogs(List<UUID> userTaskIds, UUID userId) {
        return taskLogUseCase.createLogs(userTaskIds, userId);
    }

    public UserTaskLog createManualLog(UUID userTaskId, UUID userId) {
        return taskLogUseCase.createManualLog(userTaskId, userId);
    }

    @Transactional
    public UserTaskLog updateStatus(UUID logId, TaskLogStatus newStatus, UUID userId) {
        return taskLogUseCase.updateStatus(logId, newStatus, userId);
    }

    @Transactional
    public BulkPostponeResult bulkPostpone(UUID userId, LocalDate targetDate) {
        return taskLogUseCase.bulkPostpone(userId, targetDate);
    }
}
