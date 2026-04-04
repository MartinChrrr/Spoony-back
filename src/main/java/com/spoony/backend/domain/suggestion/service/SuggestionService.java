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
import com.spoony.backend.domain.shared.exception.EnergyNotDeclaredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SuggestionService implements SuggestionUseCase {

    private static final Logger log = LoggerFactory.getLogger(SuggestionService.class);

    private final TaskPort taskPort;
    private final EnergyPort energyPort;
    private final SuggestionPort suggestionPort;
    private final SuggestionStrategy strategy;

    public SuggestionService(TaskPort taskPort, EnergyPort energyPort,
                             SuggestionPort suggestionPort, SuggestionStrategy strategy) {
        this.taskPort = taskPort;
        this.energyPort = energyPort;
        this.suggestionPort = suggestionPort;
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

        List<UUID> taskIds = activeTasks.stream().map(UserTask::getId).toList();
        Map<UUID, LocalDateTime> lastCompletions = suggestionPort.findLastCompletionDates(userId, taskIds);

        List<Suggestion> suggestions = strategy.suggest(activeTasks, availableSpoons, lastCompletions);
        log.info("Suggestions generated userId={} count={} availableSpoons={}", userId, suggestions.size(), availableSpoons);

        return suggestions;
    }
}
