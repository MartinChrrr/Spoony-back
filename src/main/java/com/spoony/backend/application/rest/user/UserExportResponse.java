package com.spoony.backend.application.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Export complet des données utilisateur (RGPD Art. 15/20)")
public class UserExportResponse {

    @Schema(description = "Profil utilisateur")
    private UserProfile profile;

    @Schema(description = "Tâches utilisateur")
    private List<ExportedTask> tasks;

    @Schema(description = "Logs de tâches")
    private List<ExportedTaskLog> taskLogs;

    @Schema(description = "Déclarations d'énergie")
    private List<ExportedEnergy> energyDeclarations;

    public UserExportResponse() {
    }

    public UserExportResponse(UserProfile profile,
                              List<ExportedTask> tasks,
                              List<ExportedTaskLog> taskLogs,
                              List<ExportedEnergy> energyDeclarations) {
        this.profile = profile;
        this.tasks = tasks;
        this.taskLogs = taskLogs;
        this.energyDeclarations = energyDeclarations;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public List<ExportedTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<ExportedTask> tasks) {
        this.tasks = tasks;
    }

    public List<ExportedTaskLog> getTaskLogs() {
        return taskLogs;
    }

    public void setTaskLogs(List<ExportedTaskLog> taskLogs) {
        this.taskLogs = taskLogs;
    }

    public List<ExportedEnergy> getEnergyDeclarations() {
        return energyDeclarations;
    }

    public void setEnergyDeclarations(List<ExportedEnergy> energyDeclarations) {
        this.energyDeclarations = energyDeclarations;
    }

    // -------------------------------------------------------------------------

    @Schema(description = "Profil utilisateur")
    public static class UserProfile {

        @Schema(description = "Email", example = "marie@example.com")
        private String email;

        @Schema(description = "Prénom", example = "Marie")
        private String firstName;

        @Schema(description = "Date de création du compte")
        private LocalDateTime createdAt;

        public UserProfile() {
        }

        public UserProfile(String email, String firstName, LocalDateTime createdAt) {
            this.email = email;
            this.firstName = firstName;
            this.createdAt = createdAt;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    // -------------------------------------------------------------------------

    @Schema(description = "Tâche exportée")
    public static class ExportedTask {

        @Schema(description = "Identifiant")
        private UUID id;

        @Schema(description = "Nom de la tâche")
        private String name;

        @Schema(description = "Coût en cuillères")
        private int spoonCost;

        @Schema(description = "Importance")
        private String importance;

        @Schema(description = "Catégorie")
        private String category;

        @Schema(description = "Date d'échéance")
        private LocalDate dueDate;

        @Schema(description = "Notes")
        private String notes;

        @Schema(description = "Statut")
        private String status;

        @Schema(description = "Date de création")
        private LocalDateTime createdAt;

        public ExportedTask() {
        }

        public ExportedTask(UUID id,
                            String name,
                            int spoonCost,
                            String importance,
                            String category,
                            LocalDate dueDate,
                            String notes,
                            String status,
                            LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.spoonCost = spoonCost;
            this.importance = importance;
            this.category = category;
            this.dueDate = dueDate;
            this.notes = notes;
            this.status = status;
            this.createdAt = createdAt;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    // -------------------------------------------------------------------------

    @Schema(description = "Log de tâche exporté")
    public static class ExportedTaskLog {

        @Schema(description = "Identifiant")
        private UUID id;

        @Schema(description = "Identifiant de la tâche utilisateur")
        private UUID userTaskId;

        @Schema(description = "Date du log")
        private LocalDate date;

        @Schema(description = "Statut")
        private String status;

        @Schema(description = "Log suggéré automatiquement")
        private boolean suggested;

        @Schema(description = "Date de complétion")
        private LocalDateTime completedAt;

        @Schema(description = "Date de création")
        private LocalDateTime createdAt;

        public ExportedTaskLog() {
        }

        public ExportedTaskLog(UUID id,
                               UUID userTaskId,
                               LocalDate date,
                               String status,
                               boolean suggested,
                               LocalDateTime completedAt,
                               LocalDateTime createdAt) {
            this.id = id;
            this.userTaskId = userTaskId;
            this.date = date;
            this.status = status;
            this.suggested = suggested;
            this.completedAt = completedAt;
            this.createdAt = createdAt;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public UUID getUserTaskId() {
            return userTaskId;
        }

        public void setUserTaskId(UUID userTaskId) {
            this.userTaskId = userTaskId;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isSuggested() {
            return suggested;
        }

        public void setSuggested(boolean suggested) {
            this.suggested = suggested;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    // -------------------------------------------------------------------------

    @Schema(description = "Déclaration d'énergie exportée")
    public static class ExportedEnergy {

        @Schema(description = "Identifiant")
        private UUID id;

        @Schema(description = "Date")
        private LocalDate date;

        @Schema(description = "Cuillères disponibles")
        private int spoons;

        @Schema(description = "Cuillères utilisées")
        private int spoonsUsed;

        @Schema(description = "Humeur de début de journée")
        private String moodStart;

        @Schema(description = "Humeur de fin de journée")
        private String moodEnd;

        @Schema(description = "Date de création")
        private LocalDateTime createdAt;

        public ExportedEnergy() {
        }

        public ExportedEnergy(UUID id,
                              LocalDate date,
                              int spoons,
                              int spoonsUsed,
                              String moodStart,
                              String moodEnd,
                              LocalDateTime createdAt) {
            this.id = id;
            this.date = date;
            this.spoons = spoons;
            this.spoonsUsed = spoonsUsed;
            this.moodStart = moodStart;
            this.moodEnd = moodEnd;
            this.createdAt = createdAt;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public int getSpoons() {
            return spoons;
        }

        public void setSpoons(int spoons) {
            this.spoons = spoons;
        }

        public int getSpoonsUsed() {
            return spoonsUsed;
        }

        public void setSpoonsUsed(int spoonsUsed) {
            this.spoonsUsed = spoonsUsed;
        }

        public String getMoodStart() {
            return moodStart;
        }

        public void setMoodStart(String moodStart) {
            this.moodStart = moodStart;
        }

        public String getMoodEnd() {
            return moodEnd;
        }

        public void setMoodEnd(String moodEnd) {
            this.moodEnd = moodEnd;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
