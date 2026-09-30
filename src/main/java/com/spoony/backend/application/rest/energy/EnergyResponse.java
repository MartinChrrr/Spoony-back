package com.spoony.backend.application.rest.energy;

import com.spoony.backend.domain.energy.model.DailyEnergy;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Énergie quotidienne de l'utilisateur")
public class EnergyResponse {

    @Schema(description = "Identifiant unique", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Date de la déclaration", example = "2026-04-05")
    private LocalDate date;

    @Schema(description = "Nombre de cuillères déclarées", example = "7", minimum = "0", maximum = "12")
    private int spoons;

    @Schema(description = "Nombre de cuillères utilisées", example = "3", minimum = "0")
    private int spoonsUsed;

    @Schema(description = "Humeur de fin de journée", example = "GOOD", allowableValues = {"GREAT", "GOOD", "NEUTRAL", "BAD", "TERRIBLE"})
    private String moodEnd;

    @Schema(description = "Date de création")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification")
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
