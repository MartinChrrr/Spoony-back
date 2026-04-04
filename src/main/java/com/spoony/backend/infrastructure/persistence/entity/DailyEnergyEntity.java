package com.spoony.backend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_energy", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "date"})
})
public class DailyEnergyEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "spoons", nullable = false)
    private short spoons;

    @Column(name = "spoons_used", nullable = false)
    private short spoonsUsed = 0;

    @Column(name = "mood_start", length = 50)
    private String moodStart;

    @Column(name = "end_of_day_mood", length = 50)
    private String endOfDayMood;

    public DailyEnergyEntity() {
    }

    public DailyEnergyEntity(UUID userId, LocalDate date, short spoons) {
        this.userId = userId;
        this.date = date;
        this.spoons = spoons;
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

    public short getSpoons() {
        return spoons;
    }

    public void setSpoons(short spoons) {
        this.spoons = spoons;
    }

    public short getSpoonsUsed() {
        return spoonsUsed;
    }

    public void setSpoonsUsed(short spoonsUsed) {
        this.spoonsUsed = spoonsUsed;
    }

    public String getMoodStart() {
        return moodStart;
    }

    public void setMoodStart(String moodStart) {
        this.moodStart = moodStart;
    }

    public String getEndOfDayMood() {
        return endOfDayMood;
    }

    public void setEndOfDayMood(String endOfDayMood) {
        this.endOfDayMood = endOfDayMood;
    }
}
