package com.ftn.sbnz.service.dto;

import java.util.ArrayList;
import java.util.List;

public class NutritionEvaluationResponse {

    private int firedRules;
    private double targetCalories;
    private double targetProtein;
    private String dailyStatus;
    private List<String> analyticStatuses = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    private List<String> detectedPatterns = new ArrayList<>();

    public int getFiredRules() {
        return firedRules;
    }

    public void setFiredRules(int firedRules) {
        this.firedRules = firedRules;
    }

    public double getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(double targetCalories) {
        this.targetCalories = targetCalories;
    }

    public double getTargetProtein() {
        return targetProtein;
    }

    public void setTargetProtein(double targetProtein) {
        this.targetProtein = targetProtein;
    }

    public String getDailyStatus() {
        return dailyStatus;
    }

    public void setDailyStatus(String dailyStatus) {
        this.dailyStatus = dailyStatus;
    }

    public List<String> getAnalyticStatuses() {
        return analyticStatuses;
    }

    public void setAnalyticStatuses(List<String> analyticStatuses) {
        this.analyticStatuses = analyticStatuses;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public List<String> getDetectedPatterns() {
        return detectedPatterns;
    }

    public void setDetectedPatterns(List<String> detectedPatterns) {
        this.detectedPatterns = detectedPatterns;
    }
}
