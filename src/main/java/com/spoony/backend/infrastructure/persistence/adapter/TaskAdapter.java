package com.spoony.backend.infrastructure.persistence.adapter;

import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.task.model.UserTask;
import com.spoony.backend.domain.task.port.out.TaskPort;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.mapper.TaskMapper;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TaskAdapter implements TaskPort {

    private final JpaUserTaskRepository userTaskRepository;

    public TaskAdapter(JpaUserTaskRepository userTaskRepository) {
        this.userTaskRepository = userTaskRepository;
    }

    @Override
    public List<UserTask> findByUserIdAndStatus(UUID userId, TaskStatus status) {
        return userTaskRepository.findByUserIdAndStatus(userId, status).stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

    @Override
    public List<UserTask> findByUserIdAndDueDateBeforeAndStatus(UUID userId, LocalDate date, TaskStatus status) {
        return userTaskRepository.findByUserIdAndDueDateBeforeAndStatus(userId, date, status).stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserTask> findById(UUID id) {
        return userTaskRepository.findById(id)
                .map(TaskMapper::toDomain);
    }

    @Override
    public UserTask save(UserTask task) {
        UserTaskEntity entity = TaskMapper.toEntity(task);
        UserTaskEntity saved = userTaskRepository.save(entity);
        return TaskMapper.toDomain(saved);
    }

    @Override
    public List<UserTask> saveAll(List<UserTask> tasks) {
        List<UserTaskEntity> entities = tasks.stream()
                .map(TaskMapper::toEntity)
                .toList();
        List<UserTaskEntity> saved = userTaskRepository.saveAll(entities);
        return saved.stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

}
