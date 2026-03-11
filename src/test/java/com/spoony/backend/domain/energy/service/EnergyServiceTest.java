package com.spoony.backend.domain.energy.service;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.domain.energy.port.out.BulkPostponePort;
import com.spoony.backend.domain.energy.port.out.EnergyPort;
import com.spoony.backend.infrastructure.exception.EnergyAlreadyDeclaredException;
import com.spoony.backend.infrastructure.exception.EnergyNotDeclaredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyServiceTest {

    @Mock
    private EnergyPort energyPort;

    @Mock
    private BulkPostponePort bulkPostponePort;

    private EnergyService energyService;

    @BeforeEach
    void setUp() {
        energyService = new EnergyService(energyPort, bulkPostponePort);
    }

    // --- getTodayEnergy ---

    @Test
    void should_ReturnEnergy_When_GetTodayExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        DailyEnergy energy = createEnergy(userId, 8);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.of(energy));

        // Act
        DailyEnergy result = energyService.getTodayEnergy(userId);

        // Assert
        assertThat(result.getSpoons()).isEqualTo(8);
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    void should_ThrowNotDeclared_When_GetTodayNotExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> energyService.getTodayEnergy(userId))
                .isInstanceOf(EnergyNotDeclaredException.class);
    }

    // --- declareEnergy ---

    @Test
    void should_SaveAndReturn_When_DeclareValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(energyPort.save(any(DailyEnergy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DailyEnergy result = energyService.declareEnergy(8, userId);

        // Assert
        assertThat(result.getSpoons()).isEqualTo(8);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getDate()).isEqualTo(LocalDate.now());
        assertThat(result.getId()).isNotNull();
        verify(energyPort).save(any(DailyEnergy.class));
        verify(bulkPostponePort, never()).postponeAllActiveTasks(any(), any(), any());
    }

    @Test
    void should_ThrowAlreadyDeclared_When_DeclareDuplicate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        DailyEnergy existing = createEnergy(userId, 5);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> energyService.declareEnergy(8, userId))
                .isInstanceOf(EnergyAlreadyDeclaredException.class);
        verify(energyPort, never()).save(any());
    }

    @Test
    void should_BulkPostpone_When_DeclareZeroSpoons() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(energyPort.save(any(DailyEnergy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bulkPostponePort.postponeAllActiveTasks(
                eq(userId), eq(LocalDate.now()), eq(LocalDate.now().plusDays(1))))
                .thenReturn(3);

        // Act
        DailyEnergy result = energyService.declareEnergy(0, userId);

        // Assert
        assertThat(result.getSpoons()).isEqualTo(0);
        verify(bulkPostponePort).postponeAllActiveTasks(
                userId, LocalDate.now(), LocalDate.now().plusDays(1));
    }

    @Test
    void should_NotPostpone_When_DeclareNonZeroSpoons() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(energyPort.save(any(DailyEnergy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        energyService.declareEnergy(5, userId);

        // Assert
        verify(bulkPostponePort, never()).postponeAllActiveTasks(any(), any(), any());
    }

    @Test
    void should_SaveBeforePostpone_When_DeclareZeroSpoons() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(energyPort.save(any(DailyEnergy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bulkPostponePort.postponeAllActiveTasks(any(), any(), any()))
                .thenReturn(0);

        // Act
        energyService.declareEnergy(0, userId);

        // Assert
        InOrder order = inOrder(energyPort, bulkPostponePort);
        order.verify(energyPort).save(any(DailyEnergy.class));
        order.verify(bulkPostponePort).postponeAllActiveTasks(any(), any(), any());
    }

    // --- updateSpoons ---

    @Test
    void should_UpdateSpoons_When_EnergyExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        DailyEnergy existing = createEnergy(userId, 8);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.of(existing));
        when(energyPort.save(any(DailyEnergy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DailyEnergy result = energyService.updateSpoons(5, userId);

        // Assert
        assertThat(result.getSpoons()).isEqualTo(5);
        verify(energyPort).save(any(DailyEnergy.class));
    }

    @Test
    void should_ThrowNotDeclared_When_UpdateSpoonsNotExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> energyService.updateSpoons(5, userId))
                .isInstanceOf(EnergyNotDeclaredException.class);
    }

    // --- updateMood ---

    @Test
    void should_UpdateMood_When_EnergyExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        DailyEnergy existing = createEnergy(userId, 8);
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.of(existing));
        when(energyPort.save(any(DailyEnergy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DailyEnergy result = energyService.updateMood("content", userId);

        // Assert
        assertThat(result.getMoodEnd()).isEqualTo("content");
        verify(energyPort).save(any(DailyEnergy.class));
    }

    @Test
    void should_ThrowNotDeclared_When_UpdateMoodNotExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(energyPort.findByUserIdAndDate(userId, LocalDate.now()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> energyService.updateMood("content", userId))
                .isInstanceOf(EnergyNotDeclaredException.class);
    }

    // --- helper ---

    private DailyEnergy createEnergy(UUID userId, int spoons) {
        return new DailyEnergy(UUID.randomUUID(), userId, LocalDate.now(), spoons);
    }
}
