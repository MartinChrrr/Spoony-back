package com.spoony.backend.infrastructure.persistence.repository;

import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface JpaUserTaskRepository extends JpaRepository<UserTaskEntity, UUID> {

    List<UserTaskEntity> findByUserIdAndStatus(UUID userId, TaskStatus status);

    List<UserTaskEntity> findByUserIdAndDueDateBeforeAndStatus(UUID userId, LocalDate date, TaskStatus status);
}
