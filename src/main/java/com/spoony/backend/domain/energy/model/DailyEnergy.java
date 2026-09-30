package com.spoony.backend.domain.energy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class DailyEnergy {

    private UUID id;
    private UUID userId;
    private LocalDate date;
    private int spoons;
    private int spoonsUsed;
    private String moodStart;
    private String moodEnd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DailyEnergy() {
    }

    public DailyEnergy(UUID id, UUID userId, LocalDate date, int spoons) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.spoons = spoons;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
