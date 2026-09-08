package com.spoony.backend.domain.task.port.in;

import com.spoony.backend.domain.task.model.TaskFromCatalogCommand;
import com.spoony.backend.domain.task.model.UserTask;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskUseCase {

    List<UserTask> findAllActiveByUserId(UUID userId);

    List<UserTask> findOverdueByUserId(UUID userId);

    Optional<UserTask> findByIdAndUserId(UUID id, UUID userId);

    UserTask create(UserTask task, UUID userId);

    UserTask update(UUID id, UserTask task, UUID userId);

    void archive(UUID id, UUID userId);

    List<UserTask> createFromCatalog(List<TaskFromCatalogCommand> commands, UUID userId);
}
