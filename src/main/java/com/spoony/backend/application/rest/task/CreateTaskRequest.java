package com.spoony.backend.application.rest.task;

import com.spoony.backend.domain.task.model.Importance;
import com.spoony.backend.domain.task.model.UserTask;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Requête de création de tâche")
public class CreateTaskRequest {

    @Schema(description = "Nom de la tâche", example = "Faire les courses", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Le nom de la tâche est obligatoire")
    @Size(max = 255, message = "Le nom de la tâche ne doit pas dépasser 255 caractères")
    private String name;

    @Schema(description = "Coût en cuillères (défaut: 2)", example = "3", minimum = "1", maximum = "5")
    @Min(value = 1, message = "Le coût en cuillères doit être entre 1 et 5")
    @Max(value = 5, message = "Le coût en cuillères doit être entre 1 et 5")
    private Integer spoonCost;

    @Schema(description = "Niveau d'importance", example = "MEDIUM", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Le niveau d'importance doit être LOW, MEDIUM ou HIGH")
    private String importance;

    @Schema(description = "Catégorie de la tâche", example = "household", maxLength = 50)
    @Size(max = 50, message = "La catégorie ne doit pas dépasser 50 caractères")
    private String category;

    @Schema(description = "Date d'échéance (défaut: aujourd'hui)", example = "2026-04-05")
    private LocalDate dueDate;

    @Schema(description = "Notes libres", example = "Ne pas oublier le lait", maxLength = 4000)
    @Size(max = 4000, message = "Les notes ne doivent pas dépasser 4000 caractères")
    private String notes;

    public CreateTaskRequest() {
    }

    public CreateTaskRequest(String name, Integer spoonCost, String importance, String category, LocalDate dueDate, String notes) {
        this.name = name;
        this.spoonCost = spoonCost;
        this.importance = importance;
        this.category = category;
        this.dueDate = dueDate;
        this.notes = notes;
    }

    public UserTask toDomain() {
        UserTask task = new UserTask();
        task.setName(name);
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
