package com.spoony.backend.application.energy;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.domain.energy.port.in.EnergyUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyApplicationServiceTest {

    @Mock
    private EnergyUseCase energyUseCase;

    private EnergyApplicationService service;

    @BeforeEach
    void setUp() {
        service = new EnergyApplicationService(energyUseCase);
    }

    @Test
    void should_DelegateDeclareEnergy_When_Called() {
        // Arrange
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(userId, 8);
        when(energyUseCase.declareEnergy(8, userId)).thenReturn(energy);

        // Act
        DailyEnergy result = service.declareEnergy(8, userId);

        // Assert
        assertThat(result.getSpoons()).isEqualTo(8);
        verify(energyUseCase).declareEnergy(8, userId);
    }

    @Test
    void should_DelegateUpdateSpoons_When_Called() {
        // Arrange
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(userId, 5);
        when(energyUseCase.updateSpoons(5, userId)).thenReturn(energy);

        // Act
        DailyEnergy result = service.updateSpoons(5, userId);

        // Assert
        assertThat(result.getSpoons()).isEqualTo(5);
        verify(energyUseCase).updateSpoons(5, userId);
    }

    @Test
    void should_DelegateGetTodayEnergy_When_Called() {
        // Arrange
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(userId, 7);
        when(energyUseCase.getTodayEnergy(userId)).thenReturn(energy);

        // Act
        DailyEnergy result = service.getTodayEnergy(userId);

        // Assert
        assertThat(result.getSpoons()).isEqualTo(7);
        verify(energyUseCase).getTodayEnergy(userId);
    }

    @Test
    void should_DelegateUpdateMood_When_Called() {
        // Arrange
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(userId, 8);
        energy.setMoodEnd("content");
        when(energyUseCase.updateMood("content", userId)).thenReturn(energy);

        // Act
        DailyEnergy result = service.updateMood("content", userId);

        // Assert
        assertThat(result.getMoodEnd()).isEqualTo("content");
        verify(energyUseCase).updateMood("content", userId);
    }

    // --- helper ---

    private DailyEnergy createEnergy(UUID userId, int spoons) {
        return new DailyEnergy(UUID.randomUUID(), userId, LocalDate.now(), spoons);
    }
}
