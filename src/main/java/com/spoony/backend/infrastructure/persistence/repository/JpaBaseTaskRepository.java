package com.spoony.backend.infrastructure.persistence.repository;

import com.spoony.backend.infrastructure.persistence.entity.BaseTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaBaseTaskRepository extends JpaRepository<BaseTaskEntity, UUID> {

    List<BaseTaskEntity> findByActiveTrue();
}
