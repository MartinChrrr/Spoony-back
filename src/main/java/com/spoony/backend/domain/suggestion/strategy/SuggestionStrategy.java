package com.spoony.backend.domain.suggestion.strategy;

import com.spoony.backend.domain.suggestion.model.Suggestion;
import com.spoony.backend.domain.task.model.UserTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SuggestionStrategy {

    List<Suggestion> suggest(List<UserTask> tasks, int availableSpoons, Map<UUID, LocalDateTime> lastCompletions);
}
