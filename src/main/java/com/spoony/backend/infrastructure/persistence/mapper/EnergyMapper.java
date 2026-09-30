package com.spoony.backend.infrastructure.persistence.mapper;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;

public final class EnergyMapper {

    private EnergyMapper() {
    }

    public static DailyEnergy toDomain(DailyEnergyEntity entity) {
        DailyEnergy energy = new DailyEnergy();
        energy.setId(entity.getId());
        energy.setUserId(entity.getUserId());
        energy.setDate(entity.getDate());
        energy.setSpoons(entity.getSpoons());
        energy.setSpoonsUsed(entity.getSpoonsUsed());
        energy.setMoodStart(entity.getMoodStart());
        energy.setMoodEnd(entity.getEndOfDayMood());
        energy.setCreatedAt(entity.getCreatedAt());
        energy.setUpdatedAt(entity.getUpdatedAt());
        return energy;
    }

    public static DailyEnergyEntity toEntity(DailyEnergy energy) {
        DailyEnergyEntity entity = new DailyEnergyEntity();
        entity.setId(energy.getId());
        entity.setUserId(energy.getUserId());
        entity.setDate(energy.getDate());
        entity.setSpoons((short) energy.getSpoons());
        entity.setSpoonsUsed((short) energy.getSpoonsUsed());
        entity.setMoodStart(energy.getMoodStart());
        entity.setEndOfDayMood(energy.getMoodEnd());
        return entity;
    }
}
