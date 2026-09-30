package com.spoony.backend.application.rest.tasklog;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Requête de report en masse des tâches PLANNED")
public class BulkPostponeRequest {

    @Schema(description = "Date cible du report (défaut: demain)", example = "2026-04-06")
    private LocalDate targetDate;

    public BulkPostponeRequest() {
    }

    public BulkPostponeRequest(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
}
