package com.spoony.backend.domain.suggestion.port.out;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface SuggestionPort {

    Map<UUID, LocalDateTime> findLastCompletionDates(UUID userId, Collection<UUID> taskIds);
}
