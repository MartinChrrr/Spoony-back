package com.spoony.backend.domain.suggestion.service;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.domain.energy.port.out.EnergyPort;
import com.spoony.backend.domain.suggestion.model.Suggestion;
import com.spoony.backend.domain.suggestion.port.in.SuggestionUseCase;
import com.spoony.backend.domain.suggestion.port.out.SuggestionPort;
import com.spoony.backend.domain.suggestion.strategy.SuggestionStrategy;
import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.task.model.UserTask;
import com.spoony.backend.domain.task.port.out.TaskPort;
import com.spoony.backend.domain.tasklog.model.UserTaskLog;
import com.spoony.backend.domain.tasklog.port.out.TaskLogPort;
import com.spoony.backend.domain.shared.exception.EnergyNotDeclaredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SuggestionService implements SuggestionUseCase {

    private static final Logger log = LoggerFactory.getLogger(SuggestionService.class);

    private final TaskPort taskPort;
    private final EnergyPort energyPort;
    private final SuggestionPort suggestionPort;
    private final TaskLogPort taskLogPort;
    private final SuggestionStrategy strategy;

    public SuggestionService(TaskPort taskPort, EnergyPort energyPort,
                             SuggestionPort suggestionPort, TaskLogPort taskLogPort,
                             SuggestionStrategy strategy) {
        this.taskPort = taskPort;
        this.energyPort = energyPort;
        this.suggestionPort = suggestionPort;
        this.taskLogPort = taskLogPort;
        this.strategy = strategy;
    }

    @Override
    public List<Suggestion> getSuggestions(UUID userId) {
        DailyEnergy energy = energyPort.findByUserIdAndDate(userId, LocalDate.now())
                .orElseThrow(EnergyNotDeclaredException::new);

        int availableSpoons = energy.getSpoons() - energy.getSpoonsUsed();

        // Cas 0 cuillère : retourner liste vide
        if (availableSpoons <= 0) {
            log.debug("No available spoons for userId={}, returning empty suggestions", userId);
            return List.of();
        }

        List<UserTask> activeTasks = taskPort.findByUserIdAndStatus(userId, TaskStatus.ACTIVE);

        if (activeTasks.isEmpty()) {
            return List.of();
        }

        // Exclure les tâches déjà loggées aujourd'hui (PLANNED/COMPLETED/SKIPPED),
        // sinon step3 re-suggère des tâches déjà planifiées ou faites du jour (audit M5).
        Set<UUID> loggedTodayTaskIds = taskLogPort.findByUserIdAndDate(userId, LocalDate.now()).stream()
                .map(UserTaskLog::getUserTaskId)
                .collect(Collectors.toSet());

        activeTasks = activeTasks.stream()
                .filter(task -> !loggedTodayTaskIds.contains(task.getId()))
                .toList();

        if (activeTasks.isEmpty()) {
            log.debug("All active tasks already logged today for userId={}, returning empty suggestions", userId);
            return List.of();
        }

        List<UUID> taskIds = activeTasks.stream().map(UserTask::getId).toList();
        Map<UUID, LocalDateTime> lastCompletions = suggestionPort.findLastCompletionDates(userId, taskIds);

        List<Suggestion> suggestions = strategy.suggest(activeTasks, availableSpoons, lastCompletions);
        log.info("Suggestions generated userId={} count={} availableSpoons={}", userId, suggestions.size(), availableSpoons);

        return suggestions;
    }
}
