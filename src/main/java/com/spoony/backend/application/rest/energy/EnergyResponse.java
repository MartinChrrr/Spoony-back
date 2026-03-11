package com.spoony.backend.application.rest.energy;

import com.spoony.backend.domain.energy.model.DailyEnergy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class EnergyResponse {

    private UUID id;
    private LocalDate date;
    private int spoons;
    private int spoonsUsed;
    private String moodEnd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EnergyResponse() {
    }

    public static EnergyResponse fromDomain(DailyEnergy energy) {
        EnergyResponse response = new EnergyResponse();
        response.setId(energy.getId());
        response.setDate(energy.getDate());
        response.setSpoons(energy.getSpoons());
        response.setSpoonsUsed(energy.getSpoonsUsed());
        response.setMoodEnd(energy.getMoodEnd());
        response.setCreatedAt(energy.getCreatedAt());
        response.setUpdatedAt(energy.getUpdatedAt());
        return response;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
