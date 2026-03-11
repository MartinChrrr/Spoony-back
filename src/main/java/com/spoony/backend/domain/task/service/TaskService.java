package com.spoony.backend.domain.task.service;

import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.task.model.UserTask;
import com.spoony.backend.domain.task.port.in.TaskUseCase;
import com.spoony.backend.domain.task.port.out.TaskPort;
import com.spoony.backend.infrastructure.exception.TaskNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskService implements TaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskPort taskPort;

    public TaskService(TaskPort taskPort) {
        this.taskPort = taskPort;
    }

    @Override
    public List<UserTask> findAllActiveByUserId(UUID userId) {
        return taskPort.findByUserIdAndStatus(userId, TaskStatus.ACTIVE);
    }

    @Override
    public List<UserTask> findOverdueByUserId(UUID userId) {
        return taskPort.findByUserIdAndDueDateBeforeAndStatus(userId, LocalDate.now(), TaskStatus.ACTIVE);
    }

    @Override
    public Optional<UserTask> findByIdAndUserId(UUID id, UUID userId) {
        return taskPort.findById(id)
                .filter(task -> task.getUserId().equals(userId));
    }

    @Override
    public UserTask create(UserTask task, UUID userId) {
        task.setId(UUID.randomUUID());
        task.setUserId(userId);
        task.setStatus(TaskStatus.ACTIVE);

        if (task.getSpoonCost() <= 0) {
            task.setSpoonCost(2);
        }
        if (task.getImportance() == null) {
            task.setImportance(Importance.MEDIUM);
        }
        if (task.getDueDate() == null) {
            task.setDueDate(LocalDate.now());
        }

        UserTask saved = taskPort.save(task);
        log.info("Task created userId={} taskId={} name={}", userId, saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public UserTask update(UUID id, UserTask task, UUID userId) {
        UserTask existing = taskPort.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(TaskNotFoundException::new);

        if (task.getName() != null) {
            existing.setName(task.getName());
        }
        if (task.getSpoonCost() > 0) {
            existing.setSpoonCost(task.getSpoonCost());
        }
        if (task.getImportance() != null) {
            existing.setImportance(task.getImportance());
        }
        if (task.getCategory() != null) {
            existing.setCategory(task.getCategory());
        }
        if (task.getDueDate() != null) {
            existing.setDueDate(task.getDueDate());
        }
        if (task.getNotes() != null) {
            existing.setNotes(task.getNotes());
        }
        if (task.getStatus() != null) {
            existing.setStatus(task.getStatus());
        }
        if (task.getCompletedAt() != null) {
            existing.setCompletedAt(task.getCompletedAt());
        }

        UserTask saved = taskPort.save(existing);
        log.info("Task updated userId={} taskId={}", userId, id);
        return saved;
    }

    @Override
    public void delete(UUID id, UUID userId) {
        taskPort.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(TaskNotFoundException::new);

        taskPort.deleteById(id);
        log.info("Task deleted userId={} taskId={}", userId, id);
    }
}
