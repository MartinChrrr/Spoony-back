package com.spoony.backend.infrastructure.persistence.repository;

import com.spoony.backend.infrastructure.persistence.entity.BenevolentMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaBenevolentMessageRepository extends JpaRepository<BenevolentMessageEntity, UUID> {

    List<BenevolentMessageEntity> findByContextAndActiveTrue(String context);
}
