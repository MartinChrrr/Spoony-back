package com.spoony.backend.infrastructure.persistence.adapter;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;
import com.spoony.backend.domain.tasklog.port.out.TaskLogPort;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import com.spoony.backend.infrastructure.persistence.mapper.TaskLogMapper;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskLogRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TaskLogAdapter implements TaskLogPort {

    private final JpaUserTaskLogRepository taskLogRepository;
    private final JpaUserTaskRepository userTaskRepository;

    public TaskLogAdapter(JpaUserTaskLogRepository taskLogRepository, JpaUserTaskRepository userTaskRepository) {
        this.taskLogRepository = taskLogRepository;
        this.userTaskRepository = userTaskRepository;
    }

    @Override
    public List<UserTaskLog> findByUserIdAndDate(UUID userId, LocalDate date) {
        return taskLogRepository.findByUserIdAndDate(userId, date).stream()
                .map(TaskLogMapper::toDomain)
                .toList();
    }

    @Override
    public List<UserTaskLog> findByUserIdAndDateBetween(UUID userId, LocalDate from, LocalDate to) {
        return taskLogRepository.findByUserIdAndDateBetween(userId, from, to).stream()
                .map(TaskLogMapper::toDomain)
                .toList();
    }

    @Override
    public List<UserTaskLog> findByUserIdAndDateExcludeArchived(UUID userId, LocalDate date) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        return taskLogRepository.findByUserIdAndDate(userId, date).stream()
                .map(TaskLogMapper::toDomain)
                .filter(log -> !(log.getStatus() == TaskLogStatus.COMPLETED
                        && log.getCompletedAt() != null
                        && log.getCompletedAt().isBefore(cutoff)))
                .toList();
    }

    @Override
    public List<UserTaskLog> findByUserIdAndDateAndStatus(UUID userId, LocalDate date, TaskLogStatus status) {
        return taskLogRepository.findByUserIdAndDateAndStatus(userId, date, status).stream()
                .map(TaskLogMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserTaskLog> findById(UUID id) {
        return taskLogRepository.findById(id)
                .map(TaskLogMapper::toDomain);
    }

    @Override
    public UserTaskLog save(UserTaskLog log) {
        UserTaskLogEntity entity = TaskLogMapper.toEntity(log);
        UserTaskLogEntity saved = taskLogRepository.save(entity);
        return TaskLogMapper.toDomain(saved);
    }

    @Override
    public List<UserTaskLog> saveAll(List<UserTaskLog> logs) {
        List<UserTaskLogEntity> entities = logs.stream()
                .map(TaskLogMapper::toEntity)
                .toList();
        List<UserTaskLogEntity> saved = taskLogRepository.saveAll(entities);
        return saved.stream()
                .map(TaskLogMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Integer> findSpoonCostByTaskIdAndUserId(UUID taskId, UUID userId) {
        return userTaskRepository.findByIdAndUserId(taskId, userId)
                .map(entity -> (int) entity.getSpoonCost());
    }

    @Override
    public boolean existsTaskForUser(UUID taskId, UUID userId) {
        return userTaskRepository.existsByIdAndUserId(taskId, userId);
    }
}
