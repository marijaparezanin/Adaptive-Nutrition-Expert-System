package com.ftn.sbnz.model.models;

import java.time.LocalDate;

public abstract class NutritionPattern {

    private Long userId;
    private LocalDate detectedAt;
    private String description;

    public NutritionPattern() {
    }

    public NutritionPattern(Long userId, LocalDate detectedAt, String description) {
        this.userId = userId;
        this.detectedAt = detectedAt;
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDate detectedAt) {
        this.detectedAt = detectedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
