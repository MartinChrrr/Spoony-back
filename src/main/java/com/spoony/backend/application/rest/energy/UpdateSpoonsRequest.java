package com.spoony.backend.application.rest.energy;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateSpoonsRequest {

    @NotNull(message = "Le nombre de cuillères est obligatoire")
    @Min(value = 0, message = "Le nombre de cuillères doit être entre 0 et 12")
    @Max(value = 12, message = "Le nombre de cuillères doit être entre 0 et 12")
    private Integer spoons;

    public UpdateSpoonsRequest() {
    }

    public UpdateSpoonsRequest(Integer spoons) {
        this.spoons = spoons;
    }

    public Integer getSpoons() {
        return spoons;
    }

    public void setSpoons(Integer spoons) {
        this.spoons = spoons;
    }
}
