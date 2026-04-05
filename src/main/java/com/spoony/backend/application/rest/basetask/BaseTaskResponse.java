package com.spoony.backend.application.rest.basetask;

import com.spoony.backend.infrastructure.persistence.entity.BaseTaskEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Tâche prédéfinie du catalogue")
public class BaseTaskResponse {

    @Schema(description = "Identifiant unique", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Clé i18n de la tâche", example = "tasks.household.groceries")
    private String key;

    @Schema(description = "Coût en cuillères", example = "3", minimum = "1", maximum = "5")
    private int spoonCost;

    @Schema(description = "Niveau d'importance", example = "MEDIUM", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private String importance;

    @Schema(description = "Catégorie de la tâche", example = "household")
    private String category;

    public BaseTaskResponse() {
    }

    public static BaseTaskResponse fromEntity(BaseTaskEntity entity) {
        BaseTaskResponse response = new BaseTaskResponse();
        response.setId(entity.getId());
        response.setKey(entity.getTaskKey());
        response.setSpoonCost(entity.getSpoonCost());
        response.setImportance(entity.getImportance());
        response.setCategory(entity.getCategory());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getSpoonCost() {
        return spoonCost;
    }

    public void setSpoonCost(int spoonCost) {
        this.spoonCost = spoonCost;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
