package com.spoony.backend.application.rest.suggestion;

import com.spoony.backend.domain.suggestion.model.Suggestion;

import java.util.UUID;

public class SuggestionResponse {

    private UUID userTaskId;
    private String name;
    private int spoonCost;
    private String importance;
    private String reason;
    private boolean exceedsBudget;

    public SuggestionResponse() {
    }

    public static SuggestionResponse fromDomain(Suggestion suggestion) {
        SuggestionResponse response = new SuggestionResponse();
        response.setUserTaskId(suggestion.getUserTaskId());
        response.setName(suggestion.getName());
        response.setSpoonCost(suggestion.getSpoonCost());
        response.setImportance(suggestion.getImportance());
        response.setReason(suggestion.getReason());
        response.setExceedsBudget(suggestion.isExceedsBudget());
        return response;
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
