package com.spoony.backend.infrastructure.persistence.adapter;

import com.spoony.backend.domain.suggestion.port.out.SuggestionPort;
import com.spoony.backend.infrastructure.persistence.repository.JpaUserTaskLogRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SuggestionAdapter implements SuggestionPort {

    private final JpaUserTaskLogRepository taskLogRepository;

    public SuggestionAdapter(JpaUserTaskLogRepository taskLogRepository) {
        this.taskLogRepository = taskLogRepository;
    }

    @Override
    public Map<UUID, LocalDateTime> findLastCompletionDates(UUID userId, Collection<UUID> taskIds) {
        if (taskIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = taskLogRepository.findLastCompletionDates(userId, taskIds);
        Map<UUID, LocalDateTime> result = new HashMap<>();

        for (Object[] row : rows) {
            UUID taskId = (UUID) row[0];
            LocalDateTime lastCompletion = (LocalDateTime) row[1];
            result.put(taskId, lastCompletion);
        }

        return result;
    }
}
