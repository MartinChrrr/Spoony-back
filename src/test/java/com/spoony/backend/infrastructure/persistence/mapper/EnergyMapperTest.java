package com.spoony.backend.infrastructure.persistence.mapper;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EnergyMapperTest {

    @Test
    void should_ConvertEntityToDomain_When_AllFieldsPresent() {
        // Arrange
        DailyEnergyEntity entity = new DailyEnergyEntity();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setDate(LocalDate.of(2026, 3, 1));
        entity.setSpoons((short) 8);
        entity.setSpoonsUsed((short) 3);
        entity.setEndOfDayMood("content");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // Act
        DailyEnergy energy = EnergyMapper.toDomain(entity);

        // Assert
        assertThat(energy.getId()).isEqualTo(id);
        assertThat(energy.getUserId()).isEqualTo(userId);
        assertThat(energy.getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(energy.getSpoons()).isEqualTo(8);
        assertThat(energy.getSpoonsUsed()).isEqualTo(3);
        assertThat(energy.getMoodEnd()).isEqualTo("content");
        assertThat(energy.getCreatedAt()).isEqualTo(now);
        assertThat(energy.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void should_ConvertDomainToEntity_When_AllFieldsPresent() {
        // Arrange
        DailyEnergy energy = new DailyEnergy();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        energy.setId(id);
        energy.setUserId(userId);
        energy.setDate(LocalDate.of(2026, 3, 1));
        energy.setSpoons(8);
        energy.setSpoonsUsed(3);
        energy.setMoodEnd("content");

        // Act
        DailyEnergyEntity entity = EnergyMapper.toEntity(energy);

        // Assert
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(entity.getSpoons()).isEqualTo((short) 8);
        assertThat(entity.getSpoonsUsed()).isEqualTo((short) 3);
        assertThat(entity.getEndOfDayMood()).isEqualTo("content");
    }

    @Test
    void should_HandleNullMood_When_EndOfDayMoodIsNull() {
        // Arrange
        DailyEnergyEntity entity = new DailyEnergyEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setDate(LocalDate.now());
        entity.setSpoons((short) 5);
        entity.setSpoonsUsed((short) 0);
        entity.setEndOfDayMood(null);

        // Act
        DailyEnergy energy = EnergyMapper.toDomain(entity);

        // Assert
        assertThat(energy.getMoodEnd()).isNull();
    }

    @Test
    void should_HandleZeroSpoons_When_SpoonsIsZero() {
        // Arrange
        DailyEnergy energy = new DailyEnergy();
        energy.setId(UUID.randomUUID());
        energy.setUserId(UUID.randomUUID());
        energy.setDate(LocalDate.now());
        energy.setSpoons(0);
        energy.setSpoonsUsed(0);

        // Act
        DailyEnergyEntity entity = EnergyMapper.toEntity(energy);

        // Assert
        assertThat(entity.getSpoons()).isEqualTo((short) 0);
        assertThat(entity.getSpoonsUsed()).isEqualTo((short) 0);
    }
}
