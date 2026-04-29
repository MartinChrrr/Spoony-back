package com.spoony.backend.domain.energy.service;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.domain.energy.port.in.EnergyUseCase;
import com.spoony.backend.domain.energy.port.out.EnergyPort;
import com.spoony.backend.domain.shared.port.out.TaskPostponePort;
import com.spoony.backend.domain.shared.exception.EnergyAlreadyDeclaredException;
import com.spoony.backend.domain.shared.exception.EnergyNotDeclaredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.UUID;

public class EnergyService implements EnergyUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnergyService.class);

    private final EnergyPort energyPort;
    private final TaskPostponePort taskPostponePort;

    public EnergyService(EnergyPort energyPort, TaskPostponePort taskPostponePort) {
        this.energyPort = energyPort;
        this.taskPostponePort = taskPostponePort;
    }

    @Override
    public DailyEnergy getTodayEnergy(UUID userId) {
        LocalDate today = LocalDate.now();
        return energyPort.findByUserIdAndDate(userId, today)
                .orElseThrow(EnergyNotDeclaredException::new);
    }

    @Override
    public DailyEnergy declareEnergy(int spoons, UUID userId) {
        LocalDate today = LocalDate.now();

        energyPort.findByUserIdAndDate(userId, today)
                .ifPresent(existing -> {
                    throw new EnergyAlreadyDeclaredException();
                });

        DailyEnergy energy = new DailyEnergy(UUID.randomUUID(), userId, today, spoons);
        DailyEnergy saved = energyPort.save(energy);
        log.info("Energy declared userId={} spoons={} date={}", userId, spoons, today);

        if (spoons == 0) {
            LocalDate tomorrow = today.plusDays(1);
            int count = taskPostponePort.postponeAllActiveTasks(userId, today, tomorrow);
            log.info("Zero spoons: bulk postponed {} tasks userId={} to={}", count, userId, tomorrow);
        }

        return saved;
    }

    @Override
    public DailyEnergy updateSpoons(int spoons, UUID userId) {
        LocalDate today = LocalDate.now();
        DailyEnergy existing = energyPort.findByUserIdAndDate(userId, today)
                .orElseThrow(EnergyNotDeclaredException::new);

        existing.setSpoons(spoons);
        DailyEnergy saved = energyPort.save(existing);
        log.info("Spoons updated userId={} newSpoons={}", userId, spoons);
        return saved;
    }

    @Override
    public DailyEnergy updateMood(String moodEnd, UUID userId) {
        LocalDate today = LocalDate.now();
        DailyEnergy existing = energyPort.findByUserIdAndDate(userId, today)
                .orElseThrow(EnergyNotDeclaredException::new);

        existing.setMoodEnd(moodEnd);
        DailyEnergy saved = energyPort.save(existing);
        log.info("Mood updated userId={} moodEnd={}", userId, moodEnd);
        return saved;
    }
}
