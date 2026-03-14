package com.spoony.backend.application.rest.tasklog;

import java.time.LocalDate;

public class BulkPostponeRequest {

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
