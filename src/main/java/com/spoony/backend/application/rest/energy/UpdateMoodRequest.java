package com.spoony.backend.application.rest.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête de mise à jour de l'humeur de fin de journée")
public class UpdateMoodRequest {

    @Schema(description = "Humeur de fin de journée", example = "GOOD", allowableValues = {"GREAT", "GOOD", "NEUTRAL", "BAD", "TERRIBLE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "L'humeur de fin de journée est obligatoire")
    private String moodEnd;

    public UpdateMoodRequest() {
    }

    public UpdateMoodRequest(String moodEnd) {
        this.moodEnd = moodEnd;
    }

    public String getMoodEnd() {
        return moodEnd;
    }

    public void setMoodEnd(String moodEnd) {
        this.moodEnd = moodEnd;
    }
}
