package com.spoony.backend.application.rest.basetask;

import com.spoony.backend.infrastructure.persistence.entity.BaseTaskEntity;

import java.util.UUID;

public class BaseTaskResponse {

    private UUID id;
    private String key;
    private int spoonCost;
    private String importance;
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
