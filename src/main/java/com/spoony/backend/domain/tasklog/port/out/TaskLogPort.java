package com.spoony.backend.domain.tasklog.port.out;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.domain.tasklog.model.TaskSnapshot;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskLogPort {

    List<UserTaskLog> findByUserIdAndDate(UUID userId, LocalDate date);

    List<UserTaskLog> findByUserIdAndDateBetween(UUID userId, LocalDate from, LocalDate to);

    List<UserTaskLog> findByUserIdAndDateExcludeArchived(UUID userId, LocalDate date);

    List<UserTaskLog> findByUserIdAndDateAndStatus(UUID userId, LocalDate date, TaskLogStatus status);

    Optional<UserTaskLog> findById(UUID id);

    UserTaskLog save(UserTaskLog log);

    UserTaskLog saveAndFlush(UserTaskLog log);

    List<UserTaskLog> saveAll(List<UserTaskLog> logs);

    /**
     * Returns immutable data to copy into a new log, only when the task belongs
     * to the user and is still active. Empty also protects against ID enumeration.
     */
    Optional<TaskSnapshot> findActiveTaskSnapshotForUser(UUID taskId, UUID userId);
}
