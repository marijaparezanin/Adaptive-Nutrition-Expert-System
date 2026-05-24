package com.ftn.sbnz.model.models;

import java.time.LocalDate;

public class LateNightEatingPattern extends NutritionPattern {

    private int lateMealCount;

    public LateNightEatingPattern() {
    }

    public LateNightEatingPattern(Long userId, LocalDate detectedAt, int lateMealCount) {
        super(userId, detectedAt, "Late meals detected " + lateMealCount + " times");
        this.lateMealCount = lateMealCount;
    }

    public int getLateMealCount() {
        return lateMealCount;
    }

    public void setLateMealCount(int lateMealCount) {
        this.lateMealCount = lateMealCount;
    }
}
