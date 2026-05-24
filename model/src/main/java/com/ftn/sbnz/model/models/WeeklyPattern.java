package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.WeightTrend;

public class WeeklyPattern {

    private String weekId;
    private int skippedMealCount;
    private int lateMealCount;
    private double totalCalories;
    private double averageCalories;
    private WeightTrend weightTrend;
    private double proteinTrend;
    private Long userId;
    private double weightVarianceKg;

    public WeeklyPattern() {
    }

    public WeeklyPattern(String weekId,
                         int skippedMealCount,
                         int lateMealCount,
                         double totalCalories,
                         double averageCalories,
                         WeightTrend weightTrend,
                         double proteinTrend,
                         Long userId) {
        this.weekId = weekId;
        this.skippedMealCount = skippedMealCount;
        this.lateMealCount = lateMealCount;
        this.totalCalories = totalCalories;
        this.averageCalories = averageCalories;
        this.weightTrend = weightTrend;
        this.proteinTrend = proteinTrend;
        this.userId = userId;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public int getSkippedMealCount() {
        return skippedMealCount;
    }

    public void setSkippedMealCount(int skippedMealCount) {
        this.skippedMealCount = skippedMealCount;
    }

    public int getLateMealCount() {
        return lateMealCount;
    }

    public void setLateMealCount(int lateMealCount) {
        this.lateMealCount = lateMealCount;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
    }

    public double getAverageCalories() {
        return averageCalories;
    }

    public void setAverageCalories(double averageCalories) {
        this.averageCalories = averageCalories;
    }

    public WeightTrend getWeightTrend() {
        return weightTrend;
    }

    public void setWeightTrend(WeightTrend weightTrend) {
        this.weightTrend = weightTrend;
    }

    public double getProteinTrend() {
        return proteinTrend;
    }

    public void setProteinTrend(double proteinTrend) {
        this.proteinTrend = proteinTrend;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public double getWeightVarianceKg() {
        return weightVarianceKg;
    }

    public void setWeightVarianceKg(double weightVarianceKg) {
        this.weightVarianceKg = weightVarianceKg;
    }
}
