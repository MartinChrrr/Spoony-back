package com.spoony.backend.infrastructure.persistence.adapter;

import com.spoony.backend.domain.shared.port.out.TaskPostponePort;
import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskEntity;
import com.spoony.backend.infrastructure.persistence.entity.UserTaskLogEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskLogRepository;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class TaskPostponeAdapter implements TaskPostponePort {

    private final JpaUserTaskRepository userTaskRepository;
    private final JpaUserTaskLogRepository userTaskLogRepository;

    public TaskPostponeAdapter(JpaUserTaskRepository userTaskRepository,
                               JpaUserTaskLogRepository userTaskLogRepository) {
        this.userTaskRepository = userTaskRepository;
        this.userTaskLogRepository = userTaskLogRepository;
    }

    /**
     * Reporte toutes les tâches actives dues jusqu'à fromDate inclus (en retard + du jour)
     * vers toDate (ADR-008 : sémantique due_date <= fromDate).
     * Supprime également les UserTaskLog PLANNED du jour fromDate
     * pour que la liste « Tâches du jour » se vide immédiatement.
     * Les logs COMPLETED et SKIPPED ne sont pas touchés.
     */
    @Override
    public int postponeAllActiveTasks(UUID userId, LocalDate fromDate, LocalDate toDate) {
        // 1. Décaler la dueDate des UserTask actives dues jusqu'à aujourd'hui inclus
        List<UserTaskEntity> activeTasks = userTaskRepository
                .findByUserIdAndStatusAndDueDateLessThanEqual(userId, TaskStatus.ACTIVE, fromDate);

        for (UserTaskEntity task : activeTasks) {
            task.setDueDate(toDate);
        }
        userTaskRepository.saveAll(activeTasks);

        // 2. Supprimer les UserTaskLog PLANNED du jour fromDate
        //    (COMPLETED et SKIPPED restent intacts)
        List<UserTaskLogEntity> plannedLogs = userTaskLogRepository
                .findByUserIdAndDateAndStatus(userId, fromDate, TaskLogStatus.PLANNED);
        userTaskLogRepository.deleteAll(plannedLogs);

        return activeTasks.size();
    }
}
