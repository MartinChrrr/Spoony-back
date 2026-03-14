package com.spoony.backend.domain.suggestion.service;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.domain.energy.port.out.EnergyPort;
import com.spoony.backend.domain.suggestion.model.Suggestion;
import com.spoony.backend.domain.suggestion.port.out.SuggestionPort;
import com.spoony.backend.domain.suggestion.strategy.SuggestionStrategy;
import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.TaskStatus;
import com.spoony.backend.domain.task.model.UserTask;
import com.spoony.backend.domain.task.port.out.TaskPort;
import com.spoony.backend.infrastructure.exception.EnergyNotDeclaredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock
    private TaskPort taskPort;

    @Mock
    private EnergyPort energyPort;

    @Mock
    private SuggestionPort suggestionPort;

    @Mock
    private SuggestionStrategy strategy;

    private SuggestionService suggestionService;

    @BeforeEach
    void setUp() {
        suggestionService = new SuggestionService(taskPort, energyPort, suggestionPort, strategy);
    }

    @Test
    void should_ThrowEnergyNotDeclared_When_NoEnergy() {
        UUID userId = UUID.randomUUID();
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> suggestionService.getSuggestions(userId))
                .isInstanceOf(EnergyNotDeclaredException.class);
    }

    @Test
    void should_ReturnEmpty_When_ZeroSpoons() {
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(0, 0);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.of(energy));

        List<Suggestion> result = suggestionService.getSuggestions(userId);

        assertThat(result).isEmpty();
        verify(taskPort, never()).findByUserIdAndStatus(any(), any());
    }

    @Test
    void should_ReturnEmpty_When_AllSpoonsUsed() {
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(5, 5);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.of(energy));

        List<Suggestion> result = suggestionService.getSuggestions(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void should_ReturnEmpty_When_NoActiveTasks() {
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(8, 2);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.of(energy));
        when(taskPort.findByUserIdAndStatus(userId, TaskStatus.ACTIVE)).thenReturn(List.of());

        List<Suggestion> result = suggestionService.getSuggestions(userId);

        assertThat(result).isEmpty();
        verify(strategy, never()).suggest(any(), anyInt(), any());
    }

    @Test
    void should_CallStrategyWithAvailableSpoons() {
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(8, 3);
        UserTask task = createTask(userId);

        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.of(energy));
        when(taskPort.findByUserIdAndStatus(userId, TaskStatus.ACTIVE)).thenReturn(List.of(task));
        when(suggestionPort.findLastCompletionDates(eq(userId), any())).thenReturn(Map.of());
        when(strategy.suggest(anyList(), eq(5), anyMap())).thenReturn(List.of(
                new Suggestion(task.getId(), task.getName(), task.getSpoonCost(), "MEDIUM", "reason", false)
        ));

        List<Suggestion> result = suggestionService.getSuggestions(userId);

        assertThat(result).hasSize(1);
        verify(strategy).suggest(anyList(), eq(5), anyMap()); // 8 - 3 = 5
    }

    @Test
    void should_FetchLastCompletionDates() {
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(8, 0);
        UserTask task = createTask(userId);

        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.of(energy));
        when(taskPort.findByUserIdAndStatus(userId, TaskStatus.ACTIVE)).thenReturn(List.of(task));
        when(suggestionPort.findLastCompletionDates(eq(userId), any())).thenReturn(
                Map.of(task.getId(), LocalDateTime.now().minusDays(3))
        );
        when(strategy.suggest(any(), anyInt(), any())).thenReturn(List.of());

        suggestionService.getSuggestions(userId);

        verify(suggestionPort).findLastCompletionDates(eq(userId), any());
    }

    @Test
    void should_ReturnStrategySuggestions() {
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(8, 0);
        UserTask t1 = createTask(userId);
        UserTask t2 = createTask(userId);

        Suggestion s1 = new Suggestion(t1.getId(), t1.getName(), 2, "HIGH", "reason1", false);
        Suggestion s2 = new Suggestion(t2.getId(), t2.getName(), 3, "MEDIUM", "reason2", true);

        when(energyPort.findByUserIdAndDate(userId, LocalDate.now())).thenReturn(Optional.of(energy));
        when(taskPort.findByUserIdAndStatus(userId, TaskStatus.ACTIVE)).thenReturn(List.of(t1, t2));
        when(suggestionPort.findLastCompletionDates(eq(userId), any())).thenReturn(Map.of());
        when(strategy.suggest(any(), anyInt(), any())).thenReturn(List.of(s1, s2));

        List<Suggestion> result = suggestionService.getSuggestions(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo(t1.getName());
        assertThat(result.get(1).isExceedsBudget()).isTrue();
    }

    // --- helpers ---

    private DailyEnergy createEnergy(int spoons, int spoonsUsed) {
        DailyEnergy energy = new DailyEnergy();
        energy.setId(UUID.randomUUID());
        energy.setDate(LocalDate.now());
        energy.setSpoons(spoons);
        energy.setSpoonsUsed(spoonsUsed);
        return energy;
    }

    private UserTask createTask(UUID userId) {
        UserTask task = new UserTask();
        task.setId(UUID.randomUUID());
        task.setUserId(userId);
        task.setName("Task-" + UUID.randomUUID().toString().substring(0, 4));
        task.setSpoonCost(2);
        task.setImportance(Importance.MEDIUM);
        return task;
    }
}
