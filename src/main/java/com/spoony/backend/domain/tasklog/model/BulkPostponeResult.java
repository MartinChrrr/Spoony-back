package com.spoony.backend.domain.tasklog.model;

import java.time.LocalDate;

public class BulkPostponeResult {

    private int postponedCount;
    private LocalDate newDate;

    public BulkPostponeResult() {
    }

    public BulkPostponeResult(int postponedCount, LocalDate newDate) {
        this.postponedCount = postponedCount;
        this.newDate = newDate;
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
