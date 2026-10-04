package com.spoony.backend.infrastructure.persistence.repository;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface JpaUserTaskLogRepository extends JpaRepository<UserTaskLogEntity, UUID> {

    List<UserTaskLogEntity> findByUserIdAndDate(UUID userId, LocalDate date);

    List<UserTaskLogEntity> findByUserIdAndDateBetween(UUID userId, LocalDate from, LocalDate to);

    List<UserTaskLogEntity> findByUserIdAndDateAndStatus(UUID userId, LocalDate date, TaskLogStatus status);

    List<UserTaskLogEntity> findByUserIdAndStatusAndCompletedAtBefore(UUID userId, TaskLogStatus status, LocalDateTime cutoff);

    @Query("SELECT utl.userTaskId, MAX(utl.completedAt) FROM UserTaskLogEntity utl " +
            "WHERE utl.userId = :userId AND utl.status = 'COMPLETED' AND utl.userTaskId IN :taskIds " +
            "GROUP BY utl.userTaskId")
    List<Object[]> findLastCompletionDates(@Param("userId") UUID userId, @Param("taskIds") Collection<UUID> taskIds);

    List<UserTaskLogEntity> findByUserId(UUID userId);
}
