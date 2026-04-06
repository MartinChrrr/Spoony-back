package com.spoony.backend.infrastructure.persistence.repository;

import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaDailyEnergyRepository extends JpaRepository<DailyEnergyEntity, UUID> {

    Optional<DailyEnergyEntity> findByUserIdAndDate(UUID userId, LocalDate date);

    List<DailyEnergyEntity> findByUserId(UUID userId);
}
