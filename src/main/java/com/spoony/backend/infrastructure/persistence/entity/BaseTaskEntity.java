package com.spoony.backend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "base_tasks")
public class BaseTaskEntity extends BaseEntity {

    @Column(name = "task_key", nullable = false, unique = true, length = 100)
    private String taskKey;

    @Column(name = "spoon_cost", nullable = false)
    private short spoonCost;

    @Column(name = "importance", nullable = false, length = 10)
    private String importance;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public BaseTaskEntity() {
    }

    public BaseTaskEntity(String taskKey, short spoonCost, String importance, String category) {
        this.taskKey = taskKey;
        this.spoonCost = spoonCost;
        this.importance = importance;
        this.category = category;
        this.active = true;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public short getSpoonCost() {
        return spoonCost;
    }

    public void setSpoonCost(short spoonCost) {
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
