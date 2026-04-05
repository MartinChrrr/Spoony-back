package com.spoony.backend.application.rest.task;

import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.UserTask;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

@Schema(description = "Requête de mise à jour de tâche (champs optionnels)")
public class UpdateTaskRequest {

    @Schema(description = "Nouveau nom de la tâche", example = "Faire les courses bio")
    private String name;

    @Schema(description = "Nouveau coût en cuillères", example = "2", minimum = "1", maximum = "5")
    @Min(value = 1, message = "Le coût en cuillères doit être entre 1 et 5")
    @Max(value = 5, message = "Le coût en cuillères doit être entre 1 et 5")
    private Integer spoonCost;

    @Schema(description = "Nouveau niveau d'importance", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private String importance;

    @Schema(description = "Nouvelle catégorie", example = "household")
    private String category;

    @Schema(description = "Nouvelle date d'échéance", example = "2026-04-06")
    private LocalDate dueDate;

    @Schema(description = "Nouvelles notes", example = "Ajouter des fruits")
    private String notes;

    public UpdateTaskRequest() {
    }

    public UserTask toDomain() {
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide");
        }
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
