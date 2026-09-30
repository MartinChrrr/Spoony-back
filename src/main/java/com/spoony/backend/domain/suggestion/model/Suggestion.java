package com.spoony.backend.domain.suggestion.model;

import java.util.UUID;

public class Suggestion {

    private UUID userTaskId;
    private String name;
    private int spoonCost;
    private String importance;
    private String reason;
    private boolean exceedsBudget;

    public Suggestion() {
    }

    public Suggestion(UUID userTaskId, String name, int spoonCost, String importance, String reason, boolean exceedsBudget) {
        this.userTaskId = userTaskId;
        this.name = name;
        this.spoonCost = spoonCost;
        this.importance = importance;
        this.reason = reason;
        this.exceedsBudget = exceedsBudget;
    }

    public UUID getUserTaskId() {
        return userTaskId;
    }

    public void setUserTaskId(UUID userTaskId) {
        this.userTaskId = userTaskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isExceedsBudget() {
        return exceedsBudget;
    }

    public void setExceedsBudget(boolean exceedsBudget) {
        this.exceedsBudget = exceedsBudget;
    }
}
