package com.spoony.backend.domain.tasklog.port.out;

import com.spoony.backend.domain.tasklog.model.TaskLogStatus;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskLogPort {

    List<UserTaskLog> findByUserIdAndDate(UUID userId, LocalDate date);

    List<UserTaskLog> findByUserIdAndDateBetween(UUID userId, LocalDate from, LocalDate to);

    List<UserTaskLog> findByUserIdAndDateExcludeArchived(UUID userId, LocalDate date);

    List<UserTaskLog> findByUserIdAndDateAndStatus(UUID userId, LocalDate date, TaskLogStatus status);

    Optional<UserTaskLog> findById(UUID id);

    UserTaskLog save(UserTaskLog log);

    List<UserTaskLog> saveAll(List<UserTaskLog> logs);

    /**
     * Retourne le coût en cuillères de la tâche UNIQUEMENT si elle appartient à
     * {@code userId}. Empêche l'IDOR : on ne révèle/comptabilise jamais le coût
     * d'une tâche d'autrui. Optional.empty() = tâche inexistante OU pas la sienne.
     */
    Optional<Integer> findSpoonCostByTaskIdAndUserId(UUID taskId, UUID userId);

    /**
     * Vrai si la tâche existe ET appartient à {@code userId}. Utilisé à la
     * création de log pour vérifier la propriété avant d'insérer.
     */
    boolean existsTaskForUser(UUID taskId, UUID userId);
}
