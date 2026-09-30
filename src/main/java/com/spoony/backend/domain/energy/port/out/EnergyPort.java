package com.spoony.backend.domain.energy.port.out;

import com.spoony.backend.domain.energy.model.DailyEnergy;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface EnergyPort {

    Optional<DailyEnergy> findByUserIdAndDate(UUID userId, LocalDate date);

    DailyEnergy save(DailyEnergy energy);

    /**
     * Atomically applies a signed delta while refusing a negative final balance.
     */
    boolean adjustSpoonsUsed(UUID userId, LocalDate date, int delta);
}
