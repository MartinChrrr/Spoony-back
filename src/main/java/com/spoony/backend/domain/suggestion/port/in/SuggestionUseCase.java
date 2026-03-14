package com.spoony.backend.domain.suggestion.port.in;

import com.spoony.backend.domain.suggestion.model.Suggestion;

import java.util.List;
import java.util.UUID;

public interface SuggestionUseCase {

    List<Suggestion> getSuggestions(UUID userId);
}
