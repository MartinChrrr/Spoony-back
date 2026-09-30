package com.spoony.backend.domain.task.model;

import java.util.UUID;

public class BaseTaskInfo {

    private UUID id;
    private String taskKey;
    private int spoonCost;
    private String importance;
    private String category;

    public BaseTaskInfo() {
    }

    public BaseTaskInfo(UUID id, String taskKey, int spoonCost, String importance, String category) {
        this.id = id;
        this.taskKey = taskKey;
        this.spoonCost = spoonCost;
        this.importance = importance;
        this.category = category;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
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
