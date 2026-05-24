package com.ftn.sbnz.model.models;

import java.time.LocalDate;

public class BreakfastSkippingPattern extends NutritionPattern {

    private int skippedDays;

    public BreakfastSkippingPattern() {
    }

    public BreakfastSkippingPattern(Long userId, LocalDate detectedAt, int skippedDays) {
        super(userId, detectedAt, "Breakfast skipped for " + skippedDays + " days");
        this.skippedDays = skippedDays;
    }

    public int getSkippedDays() {
        return skippedDays;
    }

    public void setSkippedDays(int skippedDays) {
        this.skippedDays = skippedDays;
    }
}
