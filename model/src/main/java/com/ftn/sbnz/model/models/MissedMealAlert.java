package com.ftn.sbnz.model.models;

import java.time.LocalDate;

public class MissedMealAlert extends NutritionPattern {

    private String missedCategory;
    private int hoursPastExpected;

    public MissedMealAlert() {}

    public MissedMealAlert(Long userId, LocalDate detectedAt, String missedCategory, int hoursPastExpected) {
        super(userId, detectedAt, "No " + missedCategory + " logged - " + hoursPastExpected + "h past expected time");
        this.missedCategory = missedCategory;
        this.hoursPastExpected = hoursPastExpected;
    }

    public String getMissedCategory() { return missedCategory; }
    public void setMissedCategory(String missedCategory) { this.missedCategory = missedCategory; }
    public int getHoursPastExpected() { return hoursPastExpected; }
    public void setHoursPastExpected(int hoursPastExpected) { this.hoursPastExpected = hoursPastExpected; }
}
