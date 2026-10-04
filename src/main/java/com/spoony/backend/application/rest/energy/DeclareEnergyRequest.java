package com.spoony.backend.application.rest.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requête de déclaration d'énergie quotidienne")
public class DeclareEnergyRequest {

    @Schema(description = "Nombre de cuillères disponibles (0-12)", example = "7", minimum = "0", maximum = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Le nombre de cuillères est obligatoire")
    @Min(value = 0, message = "Le nombre de cuillères doit être entre 0 et 12")
    @Max(value = 12, message = "Le nombre de cuillères doit être entre 0 et 12")
    private Integer spoons;

    public DeclareEnergyRequest() {
    }

    public DeclareEnergyRequest(Integer spoons) {
        this.spoons = spoons;
    }

    public Integer getSpoons() {
        return spoons;
    }

    public void setSpoons(Integer spoons) {
        this.spoons = spoons;
    }
}
