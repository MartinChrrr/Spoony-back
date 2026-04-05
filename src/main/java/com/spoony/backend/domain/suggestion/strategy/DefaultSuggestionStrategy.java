package com.spoony.backend.domain.suggestion.strategy;

import com.spoony.backend.domain.suggestion.model.Suggestion;
import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.UserTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DefaultSuggestionStrategy implements SuggestionStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultSuggestionStrategy.class);

    @Override
    public List<Suggestion> suggest(List<UserTask> tasks, int availableSpoons, Map<UUID, LocalDateTime> lastCompletions) {

        // Filter tasks whose individual cost fits the budget
        List<UserTask> eligible = tasks.stream()
                .filter(t -> t.getSpoonCost() <= availableSpoons)
                .toList();

        // Score and sort DESC
        List<ScoredTask> scored = eligible.stream()
                .map(task -> {
                    long daysSinceCompletion = computeDaysSinceCompletion(task.getId(), lastCompletions);
                    double frequencyWeight = computeFrequencyWeight(daysSinceCompletion);
                    int importanceValue = importanceToValue(task.getImportance());
                    double score = (importanceValue * 3.0) + (daysSinceCompletion * frequencyWeight);
                    String reason = buildReason(task.getImportance(), daysSinceCompletion);

                    log.debug("Task={} importance={} daysSince={} freqWeight={} score={}",
                            task.getName(), task.getImportance(), daysSinceCompletion, frequencyWeight, score);

                    return new ScoredTask(task, score, reason);
                })
                .sorted(Comparator.comparingDouble(ScoredTask::score).reversed())
                .toList();

        // Budget tracking for exceedsBudget flag
        List<Suggestion> suggestions = new ArrayList<>();
        int cumulativeCost = 0;

        for (ScoredTask st : scored) {
            cumulativeCost += st.task().getSpoonCost();
            boolean exceeds = cumulativeCost > availableSpoons;

            Suggestion suggestion = new Suggestion(
                    st.task().getId(),
                    st.task().getName(),
                    st.task().getSpoonCost(),
                    st.task().getImportance().name(),
                    st.reason(),
                    exceeds
            );
            suggestions.add(suggestion);
        }

        return suggestions;
    }

    long computeDaysSinceCompletion(UUID taskId, Map<UUID, LocalDateTime> lastCompletions) {
        LocalDateTime last = lastCompletions.get(taskId);
        if (last == null) {
            return 30; // never completed → treat as 30 days
        }
        return ChronoUnit.DAYS.between(last, LocalDateTime.now());
    }

    double computeFrequencyWeight(long daysSinceCompletion) {
        if (daysSinceCompletion <= 1) return 2.0;
        if (daysSinceCompletion <= 3) return 1.5;
        if (daysSinceCompletion <= 7) return 1.0;
        return 0.5;
    }

    int importanceToValue(Importance importance) {
        return switch (importance) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
    }

    private String buildReason(Importance importance, long daysSinceCompletion) {
        String level = importance.name();

        if (daysSinceCompletion >= 30) {
            return level + ".NEVER_COMPLETED";
        }
        if (daysSinceCompletion == 0) {
            return level + ".COMPLETED_TODAY";
        }
        if (daysSinceCompletion == 1) {
            return level + ".COMPLETED_YESTERDAY";
        }
        return level + ".NOT_DONE_SINCE:days=" + daysSinceCompletion;
    }

    private record ScoredTask(UserTask task, double score, String reason) {
    }
}
