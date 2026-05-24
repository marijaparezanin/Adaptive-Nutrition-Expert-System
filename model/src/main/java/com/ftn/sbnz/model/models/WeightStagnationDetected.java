package com.ftn.sbnz.model.models;

import java.time.LocalDate;

public class WeightStagnationDetected extends NutritionPattern {

    private double varianceKg;

    public WeightStagnationDetected() {
    }

    public WeightStagnationDetected(Long userId, LocalDate detectedAt, double varianceKg) {
        super(userId, detectedAt, "Weight variance below " + varianceKg + "kg");
        this.varianceKg = varianceKg;
    }

    public double getVarianceKg() {
        return varianceKg;
    }

    public void setVarianceKg(double varianceKg) {
        this.varianceKg = varianceKg;
    }
}
