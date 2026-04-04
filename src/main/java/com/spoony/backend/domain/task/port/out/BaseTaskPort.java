package com.spoony.backend.domain.task.port.out;

import com.spoony.backend.domain.task.model.BaseTaskInfo;

import java.util.Optional;
import java.util.UUID;

public interface BaseTaskPort {

    Optional<BaseTaskInfo> findById(UUID id);
}
