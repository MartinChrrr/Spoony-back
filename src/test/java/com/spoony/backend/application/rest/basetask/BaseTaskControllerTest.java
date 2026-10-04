package com.spoony.backend.application.rest.basetask;

import com.spoony.backend.infrastructure.persistence.entity.BaseTaskEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaBaseTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseTaskControllerTest {

    @Mock
    private JpaBaseTaskRepository baseTaskRepository;

    private BaseTaskController controller;

    @BeforeEach
    void setUp() {
        controller = new BaseTaskController(baseTaskRepository);
    }

    @Test
    void should_ReturnAllActiveTasks() {
        BaseTaskEntity t1 = new BaseTaskEntity("tasks.hygiene.shower", (short) 2, "MEDIUM", "HYGIENE");
        BaseTaskEntity t2 = new BaseTaskEntity("tasks.menage.dishes", (short) 1, "LOW", "MENAGE");
        when(baseTaskRepository.findByActiveTrue()).thenReturn(List.of(t1, t2));

        var response = controller.getBaseTasks(null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData()).hasSize(2);
        verify(baseTaskRepository).findByActiveTrue();
        verify(baseTaskRepository, never()).findByActiveTrueAndCategory(any());
    }

    @Test
    void should_FilterByCategory() {
        BaseTaskEntity t1 = new BaseTaskEntity("tasks.hygiene.shower", (short) 2, "MEDIUM", "HYGIENE");
        when(baseTaskRepository.findByActiveTrueAndCategory("HYGIENE")).thenReturn(List.of(t1));

        var response = controller.getBaseTasks("HYGIENE", null);

        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getCategory()).isEqualTo("HYGIENE");
        verify(baseTaskRepository).findByActiveTrueAndCategory("HYGIENE");
    }

    @Test
    void should_ReturnEmptyForUnknownCategory() {
        when(baseTaskRepository.findByActiveTrueAndCategory("UNKNOWN")).thenReturn(List.of());

        var response = controller.getBaseTasks("UNKNOWN", null);

        assertThat(response.getBody().getData()).isEmpty();
    }

    @Test
    void should_IgnoreLocaleParam() {
        when(baseTaskRepository.findByActiveTrue()).thenReturn(List.of());

        var response = controller.getBaseTasks(null, "fr");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(baseTaskRepository).findByActiveTrue();
    }

    @Test
    void should_MapEntityFieldsCorrectly() {
        BaseTaskEntity entity = new BaseTaskEntity("tasks.sante.exercise", (short) 3, "HIGH", "SANTE");
        when(baseTaskRepository.findByActiveTrue()).thenReturn(List.of(entity));

        var response = controller.getBaseTasks(null, null);
        BaseTaskResponse dto = response.getBody().getData().get(0);

        assertThat(dto.getKey()).isEqualTo("tasks.sante.exercise");
        assertThat(dto.getSpoonCost()).isEqualTo(3);
        assertThat(dto.getImportance()).isEqualTo("HIGH");
        assertThat(dto.getCategory()).isEqualTo("SANTE");
    }
}
