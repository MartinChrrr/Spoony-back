package com.spoony.backend.application.rest.tasklog;

import com.spoony.backend.domain.tasklog.model.BulkPostponeResult;

import java.time.LocalDate;

public class BulkPostponeResponse {

    private int postponedCount;
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
