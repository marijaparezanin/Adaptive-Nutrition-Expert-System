package com.ftn.sbnz.model.models;

import java.time.LocalDate;

public class BingePattern extends NutritionPattern {

    private int mealsInWindow;
    private int windowMinutes;

    public BingePattern() {
    }

    public BingePattern(Long userId, LocalDate detectedAt, int mealsInWindow, int windowMinutes) {
        super(userId, detectedAt, mealsInWindow + " meals inside " + windowMinutes + " minutes");
        this.mealsInWindow = mealsInWindow;
        this.windowMinutes = windowMinutes;
    }

    public int getMealsInWindow() {
        return mealsInWindow;
    }

    public void setMealsInWindow(int mealsInWindow) {
        this.mealsInWindow = mealsInWindow;
    }

    public int getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(int windowMinutes) {
        this.windowMinutes = windowMinutes;
    }
}
