package com.ftn.sbnz.model.models;

import java.time.LocalDate;

public class LongFastDetected extends NutritionPattern {

    private double hoursWithoutMeal;

    public LongFastDetected() {
    }

    public LongFastDetected(Long userId, LocalDate detectedAt, double hoursWithoutMeal) {
        super(userId, detectedAt, "No meal logged for " + hoursWithoutMeal + " active hours");
        this.hoursWithoutMeal = hoursWithoutMeal;
    }

    public double getHoursWithoutMeal() {
        return hoursWithoutMeal;
    }

    public void setHoursWithoutMeal(double hoursWithoutMeal) {
        this.hoursWithoutMeal = hoursWithoutMeal;
    }
}
