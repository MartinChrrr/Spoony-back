package com.spoony.backend.domain.task.service;

import com.spoony.backend.domain.task.model.BaseTaskInfo;
import com.spoony.backend.domain.task.model.TaskFromCatalogCommand;
import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.task.model.UserTask;
import com.spoony.backend.domain.task.port.in.TaskUseCase;
import com.spoony.backend.domain.task.port.out.BaseTaskPort;
import com.spoony.backend.domain.task.port.out.TaskPort;
import com.spoony.backend.domain.shared.exception.TaskNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskService implements TaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskPort taskPort;
    private final BaseTaskPort baseTaskPort;

    public TaskService(TaskPort taskPort, BaseTaskPort baseTaskPort) {
        this.taskPort = taskPort;
        this.baseTaskPort = baseTaskPort;
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
    public void archive(UUID id, UUID userId) {
        UserTask existing = taskPort.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(TaskNotFoundException::new);

        if (existing.getStatus() != TaskStatus.ARCHIVED) {
            existing.setStatus(TaskStatus.ARCHIVED);
            taskPort.save(existing);
        }
        log.info("Task archived userId={} taskId={}", userId, id);
    }

    @Override
    public List<UserTask> createFromCatalog(List<TaskFromCatalogCommand> commands, UUID userId) {
        List<UserTask> tasks = new ArrayList<>();

        for (TaskFromCatalogCommand item : commands) {
            BaseTaskInfo baseTask = baseTaskPort.findById(item.getBaseTaskId())
                    .orElseThrow(TaskNotFoundException::new);

            UserTask task = new UserTask();
            task.setId(UUID.randomUUID());
            task.setUserId(userId);
            task.setName(item.getName() != null ? item.getName() : baseTask.getTaskKey());
            task.setSpoonCost(baseTask.getSpoonCost());
            task.setImportance(Importance.valueOf(baseTask.getImportance()));
            task.setCategory(baseTask.getCategory());
            task.setDueDate(LocalDate.now());
            task.setStatus(TaskStatus.ACTIVE);
            tasks.add(task);
        }

        List<UserTask> saved = taskPort.saveAll(tasks);
        log.info("Tasks created from catalog userId={} count={}", userId, saved.size());
        return saved;
    }
}
