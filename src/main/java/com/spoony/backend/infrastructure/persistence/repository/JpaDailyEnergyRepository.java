package com.spoony.backend.infrastructure.persistence.repository;

import com.spoony.backend.infrastructure.persistence.entity.DailyEnergyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaDailyEnergyRepository extends JpaRepository<DailyEnergyEntity, UUID> {

    Optional<DailyEnergyEntity> findByUserIdAndDate(UUID userId, LocalDate date);

    List<DailyEnergyEntity> findByUserId(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE daily_energy
            SET spoons_used = spoons_used + :delta,
                updated_at = CURRENT_TIMESTAMP
            WHERE user_id = :userId
              AND date = :date
              AND spoons_used + :delta >= 0
            """, nativeQuery = true)
    int adjustSpoonsUsed(@Param("userId") UUID userId,
                         @Param("date") LocalDate date,
                         @Param("delta") int delta);
}
