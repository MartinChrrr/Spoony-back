package com.spoony.backend.infrastructure.persistence.adapter;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.domain.energy.port.out.EnergyPort;
import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;
import com.spoony.backend.infrastructure.persistence.mapper.EnergyMapper;
import com.spoony.backend.infrastructure.persistence.repository.JpaDailyEnergyRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public class EnergyAdapter implements EnergyPort {

    private final JpaDailyEnergyRepository dailyEnergyRepository;

    public EnergyAdapter(JpaDailyEnergyRepository dailyEnergyRepository) {
        this.dailyEnergyRepository = dailyEnergyRepository;
    }

    @Override
    public Optional<DailyEnergy> findByUserIdAndDate(UUID userId, LocalDate date) {
        return dailyEnergyRepository.findByUserIdAndDate(userId, date)
                .map(EnergyMapper::toDomain);
    }

    @Override
    public DailyEnergy save(DailyEnergy energy) {
        DailyEnergyEntity entity = EnergyMapper.toEntity(energy);
        DailyEnergyEntity saved = dailyEnergyRepository.save(entity);
        return EnergyMapper.toDomain(saved);
    }

    @Override
    public boolean adjustSpoonsUsed(UUID userId, LocalDate date, int delta) {
        return dailyEnergyRepository.adjustSpoonsUsed(userId, date, delta) == 1;
    }
}
