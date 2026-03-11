package com.spoony.backend.domain.energy.port.in;

import com.spoony.backend.domain.energy.model.DailyEnergy;

import java.util.UUID;

public interface EnergyUseCase {

    DailyEnergy getTodayEnergy(UUID userId);

    DailyEnergy declareEnergy(int spoons, UUID userId);

    DailyEnergy updateSpoons(int spoons, UUID userId);

    DailyEnergy updateMood(String moodEnd, UUID userId);
}
