package com.spoony.backend.domain.task.port.out;

import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.task.model.UserTask;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskPort {

    List<UserTask> findByUserIdAndStatus(UUID userId, TaskStatus status);

    List<UserTask> findByUserIdAndDueDateBeforeAndStatus(UUID userId, LocalDate date, TaskStatus status);

    Optional<UserTask> findById(UUID id);

    List<UserTask> findBaseTasksByIds(List<UUID> baseTaskIds);

    UserTask save(UserTask task);

    List<UserTask> saveAll(List<UserTask> tasks);

    void deleteById(UUID id);
}
