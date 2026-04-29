package com.spoony.backend.infrastructure.persistence.adapter;

import com.spoony.backend.domain.shared.port.out.TaskPostponePort;
import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class TaskPostponeAdapter implements TaskPostponePort {

    private final JpaUserTaskRepository userTaskRepository;

    public TaskPostponeAdapter(JpaUserTaskRepository userTaskRepository) {
        this.userTaskRepository = userTaskRepository;
    }

    @Override
    public int postponeAllActiveTasks(UUID userId, LocalDate fromDate, LocalDate toDate) {
        List<UserTaskEntity> activeTasks = userTaskRepository
                .findByUserIdAndStatusAndDueDate(userId, TaskStatus.ACTIVE, fromDate);

        for (UserTaskEntity task : activeTasks) {
            task.setDueDate(toDate);
        }

        userTaskRepository.saveAll(activeTasks);
        return activeTasks.size();
    }
}
