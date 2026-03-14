package com.spoony.backend.domain.tasklog.port.in;

import com.spoony.backend.domain.tasklog.model.BulkPostponeResult;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskLogUseCase {

    List<UserTaskLog> getTodayLogs(UUID userId, boolean includeArchived);

    List<UserTaskLog> createLogs(List<UUID> userTaskIds, UUID userId);

    UserTaskLog createManualLog(UUID userTaskId, UUID userId);

    UserTaskLog updateStatus(UUID logId, TaskLogStatus newStatus, UUID userId);

    BulkPostponeResult bulkPostpone(UUID userId, LocalDate targetDate);
}
