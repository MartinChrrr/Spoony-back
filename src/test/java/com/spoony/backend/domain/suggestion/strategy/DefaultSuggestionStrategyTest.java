package com.spoony.backend.domain.suggestion.strategy;

import com.spoony.backend.domain.suggestion.model.Suggestion;
import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.UserTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSuggestionStrategyTest {

    private DefaultSuggestionStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DefaultSuggestionStrategy();
    }

    // --- filtering ---

    @Test
    void should_ExcludeTasksExceedingIndividualBudget() {
        UserTask cheap = createTask("Cheap", 2, Importance.MEDIUM);
        UserTask expensive = createTask("Expensive", 5, Importance.HIGH);

        List<Suggestion> result = strategy.suggest(List.of(cheap, expensive), 3, Map.of());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Cheap");
    }

    @Test
    void should_ReturnEmpty_When_NoTasksFitBudget() {
        UserTask task = createTask("Big", 5, Importance.HIGH);

        List<Suggestion> result = strategy.suggest(List.of(task), 3, Map.of());

        assertThat(result).isEmpty();
    }

    @Test
    void should_ReturnEmpty_When_ZeroSpoons() {
        UserTask task = createTask("T", 1, Importance.LOW);

        List<Suggestion> result = strategy.suggest(List.of(task), 0, Map.of());

        assertThat(result).isEmpty();
    }

    // --- scoring ---

    @Test
    void should_SortByScoreDescending() {
        UserTask high = createTask("High", 2, Importance.HIGH);
        UserTask low = createTask("Low", 2, Importance.LOW);

        List<Suggestion> result = strategy.suggest(List.of(low, high), 8, Map.of());

        assertThat(result.get(0).getName()).isEqualTo("High");
        assertThat(result.get(1).getName()).isEqualTo("Low");
    }

    @Test
    void should_ScoreHigherForOlderTasks() {
        UserTask taskA = createTask("Recent", 2, Importance.MEDIUM);
        UserTask taskB = createTask("Old", 2, Importance.MEDIUM);

        Map<UUID, LocalDateTime> completions = new HashMap<>();
        completions.put(taskA.getId(), LocalDateTime.now().minusDays(1));
        completions.put(taskB.getId(), LocalDateTime.now().minusDays(10));

        List<Suggestion> result = strategy.suggest(List.of(taskA, taskB), 8, completions);

        // Old task should score higher (same importance, more days)
        assertThat(result.get(0).getName()).isEqualTo("Old");
    }

    @Test
    void should_TreatNeverCompletedAs30Days() {
        UserTask neverDone = createTask("NeverDone", 2, Importance.MEDIUM);
        UserTask doneRecently = createTask("Recent", 2, Importance.MEDIUM);

        Map<UUID, LocalDateTime> completions = new HashMap<>();
        completions.put(doneRecently.getId(), LocalDateTime.now().minusDays(1));
        // neverDone not in map

        List<Suggestion> result = strategy.suggest(List.of(doneRecently, neverDone), 8, completions);

        assertThat(result.get(0).getName()).isEqualTo("NeverDone");
    }

    // --- exceedsBudget ---

    @Test
    void should_MarkExceedsBudgetCorrectly() {
        // Budget = 8, tasks: [3, 2, 2, 3] sorted by score
        UserTask t1 = createTask("T1", 3, Importance.HIGH);
        UserTask t2 = createTask("T2", 2, Importance.HIGH);
        UserTask t3 = createTask("T3", 2, Importance.MEDIUM);
        UserTask t4 = createTask("T4", 3, Importance.LOW);

        List<Suggestion> result = strategy.suggest(List.of(t1, t2, t3, t4), 8, Map.of());

        // After sorting by score DESC, cumulative cost determines exceedsBudget
        int cumul = 0;
        for (Suggestion s : result) {
            cumul += s.getSpoonCost();
            if (cumul <= 8) {
                assertThat(s.isExceedsBudget()).as("Task %s cumul=%d", s.getName(), cumul).isFalse();
            } else {
                assertThat(s.isExceedsBudget()).as("Task %s cumul=%d", s.getName(), cumul).isTrue();
            }
        }
    }

    @Test
    void should_AllFitBudget_When_EnoughSpoons() {
        UserTask t1 = createTask("T1", 1, Importance.HIGH);
        UserTask t2 = createTask("T2", 1, Importance.MEDIUM);

        List<Suggestion> result = strategy.suggest(List.of(t1, t2), 10, Map.of());

        assertThat(result).allSatisfy(s -> assertThat(s.isExceedsBudget()).isFalse());
    }

    // --- frequency weight ---

    @Test
    void should_Return2_When_DaysSince1() {
        assertThat(strategy.computeFrequencyWeight(1)).isEqualTo(2.0);
    }

    @Test
    void should_Return1point5_When_DaysSince3() {
        assertThat(strategy.computeFrequencyWeight(3)).isEqualTo(1.5);
    }

    @Test
    void should_Return1_When_DaysSince7() {
        assertThat(strategy.computeFrequencyWeight(7)).isEqualTo(1.0);
    }

    @Test
    void should_Return0point5_When_DaysSince30() {
        assertThat(strategy.computeFrequencyWeight(30)).isEqualTo(0.5);
    }

    // --- reason ---

    @Test
    void should_GenerateReason() {
        UserTask task = createTask("T", 2, Importance.HIGH);

        List<Suggestion> result = strategy.suggest(List.of(task), 8, Map.of());

        assertThat(result.get(0).getReason()).startsWith("HIGH.");
    }

    // --- importance value ---

    @Test
    void should_MapImportanceCorrectly() {
        assertThat(strategy.importanceToValue(Importance.LOW)).isEqualTo(1);
        assertThat(strategy.importanceToValue(Importance.MEDIUM)).isEqualTo(2);
        assertThat(strategy.importanceToValue(Importance.HIGH)).isEqualTo(3);
    }

    // --- helpers ---

    private UserTask createTask(String name, int spoonCost, Importance importance) {
        UserTask task = new UserTask();
        task.setId(UUID.randomUUID());
        task.setUserId(UUID.randomUUID());
        task.setName(name);
        task.setSpoonCost(spoonCost);
        task.setImportance(importance);
        return task;
    }
}
