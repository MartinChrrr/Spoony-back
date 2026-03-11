package com.spoony.backend.application.rest.energy;

import jakarta.validation.constraints.NotBlank;

public class UpdateMoodRequest {

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
