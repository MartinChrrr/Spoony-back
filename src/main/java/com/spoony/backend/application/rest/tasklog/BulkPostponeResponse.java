package com.spoony.backend.application.rest.tasklog;

import com.spoony.backend.domain.tasklog.model.BulkPostponeResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Résultat du report en masse")
public class BulkPostponeResponse {

    @Schema(description = "Nombre de tâches reportées", example = "5")
    private int postponedCount;

    @Schema(description = "Nouvelle date des tâches reportées", example = "2026-04-06")
    private LocalDate newDate;

    public BulkPostponeResponse() {
    }

    public static BulkPostponeResponse fromDomain(BulkPostponeResult result) {
        BulkPostponeResponse response = new BulkPostponeResponse();
        response.setPostponedCount(result.getPostponedCount());
        response.setNewDate(result.getNewDate());
        return response;
    }

    public int getPostponedCount() {
        return postponedCount;
    }

    public void setPostponedCount(int postponedCount) {
        this.postponedCount = postponedCount;
    }

    public LocalDate getNewDate() {
        return newDate;
    }

    public void setNewDate(LocalDate newDate) {
        this.newDate = newDate;
    }
}
