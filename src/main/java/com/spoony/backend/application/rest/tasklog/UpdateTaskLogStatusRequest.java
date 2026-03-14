package com.spoony.backend.application.rest.tasklog;

import jakarta.validation.constraints.NotBlank;

public class UpdateTaskLogStatusRequest {

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
