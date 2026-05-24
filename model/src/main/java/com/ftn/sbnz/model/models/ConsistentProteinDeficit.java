package com.ftn.sbnz.model.models;

import java.time.LocalDate;

public class ConsistentProteinDeficit extends NutritionPattern {

    private int daysBelowTarget;

    public ConsistentProteinDeficit() {
    }

    public ConsistentProteinDeficit(Long userId, LocalDate detectedAt, int daysBelowTarget) {
        super(userId, detectedAt, "Protein intake below 70% target for " + daysBelowTarget + " days");
        this.daysBelowTarget = daysBelowTarget;
    }

    public int getDaysBelowTarget() {
        return daysBelowTarget;
    }

    public void setDaysBelowTarget(int daysBelowTarget) {
        this.daysBelowTarget = daysBelowTarget;
    }
}
