package com.ftn.sbnz.model.models;

import java.time.LocalDate;

public class UnplannedSnackingPattern extends NutritionPattern {

    private int snackCount;

    public UnplannedSnackingPattern() {
    }

    public UnplannedSnackingPattern(Long userId, LocalDate detectedAt, int snackCount) {
        super(userId, detectedAt, "Unplanned snacks detected " + snackCount + " times");
        this.snackCount = snackCount;
    }

    public int getSnackCount() {
        return snackCount;
    }

    public void setSnackCount(int snackCount) {
        this.snackCount = snackCount;
    }
}
