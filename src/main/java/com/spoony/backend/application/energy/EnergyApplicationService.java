package com.spoony.backend.application.energy;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.domain.energy.port.in.EnergyUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for energy operations.
 * Wraps the domain use case and provides Spring-managed transactions
 * so that declareEnergy (2 writes: save energy + postpone tasks) and
 * updateSpoons-to-zero are atomic.
 * The domain service (EnergyService) stays annotation-free per hexagonal rules.
 */
@Service
public class EnergyApplicationService {

    private final EnergyUseCase energyUseCase;

    public EnergyApplicationService(EnergyUseCase energyUseCase) {
        this.energyUseCase = energyUseCase;
    }

    public DailyEnergy getTodayEnergy(UUID userId) {
        return energyUseCase.getTodayEnergy(userId);
    }

    @Transactional
    public DailyEnergy declareEnergy(int spoons, UUID userId) {
        return energyUseCase.declareEnergy(spoons, userId);
    }

    @Transactional
    public DailyEnergy updateSpoons(int spoons, UUID userId) {
        return energyUseCase.updateSpoons(spoons, userId);
    }

    public DailyEnergy updateMood(String moodEnd, UUID userId) {
        return energyUseCase.updateMood(moodEnd, userId);
    }
}
