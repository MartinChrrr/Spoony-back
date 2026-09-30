package com.spoony.backend.infrastructure.persistence.adapter;

import com.spoony.backend.domain.task.model.BaseTaskInfo;
import com.spoony.backend.domain.task.port.out.BaseTaskPort;
import com.spoony.backend.infrastructure.persistence.repository.JpaBaseTaskRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class BaseTaskAdapter implements BaseTaskPort {

    private final JpaBaseTaskRepository baseTaskRepository;

    public BaseTaskAdapter(JpaBaseTaskRepository baseTaskRepository) {
        this.baseTaskRepository = baseTaskRepository;
    }

    @Override
    public Optional<BaseTaskInfo> findById(UUID id) {
        return baseTaskRepository.findById(id)
                .map(entity -> new BaseTaskInfo(
                        entity.getId(),
                        entity.getTaskKey(),
                        entity.getSpoonCost(),
                        entity.getImportance(),
                        entity.getCategory()
                ));
    }
}
