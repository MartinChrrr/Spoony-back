package com.spoony.backend.application.rest.task;

import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.UserTask;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public class UpdateTaskRequest {

    private String name;

    @Min(value = 1, message = "Le coût en cuillères doit être entre 1 et 5")
    @Max(value = 5, message = "Le coût en cuillères doit être entre 1 et 5")
    private Integer spoonCost;

    private String importance;
    private String category;
    private LocalDate dueDate;
    private String notes;

    public UpdateTaskRequest() {
    }

    public UserTask toDomain() {
        UserTask task = new UserTask();
        if (name != null) {
            task.setName(name);
        }
        if (spoonCost != null) {
            task.setSpoonCost(spoonCost);
        }
        if (importance != null) {
            task.setImportance(Importance.valueOf(importance));
        }
        if (category != null) {
            task.setCategory(category);
        }
        if (dueDate != null) {
            task.setDueDate(dueDate);
        }
        if (notes != null) {
            task.setNotes(notes);
        }
        return task;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSpoonCost() {
        return spoonCost;
    }

    public void setSpoonCost(Integer spoonCost) {
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
