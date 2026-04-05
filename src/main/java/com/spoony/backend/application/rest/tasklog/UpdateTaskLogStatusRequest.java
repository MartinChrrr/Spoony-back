package com.spoony.backend.application.rest.tasklog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête de changement de statut d'un log")
public class UpdateTaskLogStatusRequest {

    @Schema(description = "Nouveau statut du log", example = "COMPLETED", allowableValues = {"PLANNED", "COMPLETED", "SKIPPED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Le statut est obligatoire")
    private String status;

    public UpdateTaskLogStatusRequest() {
    }

    public UpdateTaskLogStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
