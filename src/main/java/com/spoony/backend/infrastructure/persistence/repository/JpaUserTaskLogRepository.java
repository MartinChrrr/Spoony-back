package com.spoony.backend.infrastructure.persistence.repository;

import com.spoony.backend.domain.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JpaUserTaskLogRepository extends JpaRepository<UserTaskLogEntity, UUID> {

    List<UserTaskLogEntity> findByUserIdAndDate(UUID userId, LocalDate date);

    List<UserTaskLogEntity> findByUserIdAndDateAndStatus(UUID userId, LocalDate date, TaskLogStatus status);

    List<UserTaskLogEntity> findByUserIdAndStatusAndCompletedAtBefore(UUID userId, TaskLogStatus status, LocalDateTime cutoff);
}
