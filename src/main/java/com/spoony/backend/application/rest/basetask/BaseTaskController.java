package com.spoony.backend.application.rest.basetask;

import com.spoony.backend.application.rest.common.JSendResponse;
import com.spoony.backend.infrastructure.persistence.entity.BaseTaskEntity;
import com.spoony.backend.infrastructure.persistence.repository.JpaBaseTaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/base-tasks")
@Tag(name = "BaseTasks", description = "Catalogue de tâches prédéfinies")
public class BaseTaskController {

    private final JpaBaseTaskRepository baseTaskRepository;

    public BaseTaskController(JpaBaseTaskRepository baseTaskRepository) {
        this.baseTaskRepository = baseTaskRepository;
    }

    @GetMapping
    @Operation(summary = "Lister les tâches prédéfinies", description = "Retourne les tâches actives du catalogue, avec filtre optionnel par catégorie")
    @ApiResponse(responseCode = "200", description = "Tâches retournées")
    public ResponseEntity<JSendResponse<List<BaseTaskResponse>>> getBaseTasks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String locale) {

        List<BaseTaskEntity> entities;
        if (category != null && !category.isBlank()) {
            entities = baseTaskRepository.findByActiveTrueAndCategory(category);
        } else {
            entities = baseTaskRepository.findByActiveTrue();
        }

        List<BaseTaskResponse> responses = entities.stream()
                .map(BaseTaskResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(JSendResponse.success(responses));
    }
}
