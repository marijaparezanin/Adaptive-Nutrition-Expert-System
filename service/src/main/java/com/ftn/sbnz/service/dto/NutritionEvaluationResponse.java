package com.ftn.sbnz.service.dto;

import java.util.ArrayList;
import java.util.List;

public class NutritionEvaluationResponse {

    private int firedRules;
    private double bmr;
    private double targetCalories;
    private double targetProtein;
    private double targetFiber;
    private double totalCalories;
    private double totalProtein;
    private double totalFiber;
    private double caloriesRemaining;
    private double proteinRemaining;
    private double fiberRemaining;
    private int mealCount;
    private String dailyStatus;
    private List<String> analyticStatuses = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    private List<String> recommendationTypes = new ArrayList<>();
    private List<String> detectedPatterns = new ArrayList<>();
    private List<String> patternDetails = new ArrayList<>();
    private String expertOpinionSummary = "";
    private List<String> reasoningTrace = new ArrayList<>();
    private String calorieStatusReason = "";
    private String proteinStatusReason = "";
    private String fiberStatusReason = "";
    private double caloriePercentage = 0;
    private double proteinPercentage = 0;
    private double fiberPercentage = 0;

    public int getFiredRules() {
        return firedRules;
    }

    public void setFiredRules(int firedRules) {
        this.firedRules = firedRules;
    }

    public double getBmr() {
        return bmr;
    }

    public void setBmr(double bmr) {
        this.bmr = bmr;
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

    public double getTargetFiber() {
        return targetFiber;
    }

    public void setTargetFiber(double targetFiber) {
        this.targetFiber = targetFiber;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
    }

    public double getTotalProtein() {
        return totalProtein;
    }

    public void setTotalProtein(double totalProtein) {
        this.totalProtein = totalProtein;
    }

    public double getTotalFiber() {
        return totalFiber;
    }

    public void setTotalFiber(double totalFiber) {
        this.totalFiber = totalFiber;
    }

    public double getCaloriesRemaining() {
        return caloriesRemaining;
    }

    public void setCaloriesRemaining(double caloriesRemaining) {
        this.caloriesRemaining = caloriesRemaining;
    }

    public double getProteinRemaining() {
        return proteinRemaining;
    }

    public void setProteinRemaining(double proteinRemaining) {
        this.proteinRemaining = proteinRemaining;
    }

    public double getFiberRemaining() {
        return fiberRemaining;
    }

    public void setFiberRemaining(double fiberRemaining) {
        this.fiberRemaining = fiberRemaining;
    }

    public int getMealCount() {
        return mealCount;
    }

    public void setMealCount(int mealCount) {
        this.mealCount = mealCount;
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

    public List<String> getRecommendationTypes() {
        return recommendationTypes;
    }

    public void setRecommendationTypes(List<String> recommendationTypes) {
        this.recommendationTypes = recommendationTypes;
    }

    public List<String> getDetectedPatterns() {
        return detectedPatterns;
    }

    public void setDetectedPatterns(List<String> detectedPatterns) {
        this.detectedPatterns = detectedPatterns;
    }

    public List<String> getPatternDetails() {
        return patternDetails;
    }

    public void setPatternDetails(List<String> patternDetails) {
        this.patternDetails = patternDetails;
    }

    public String getExpertOpinionSummary() {
        return expertOpinionSummary;
    }

    public void setExpertOpinionSummary(String expertOpinionSummary) {
        this.expertOpinionSummary = expertOpinionSummary;
    }

    public List<String> getReasoningTrace() {
        return reasoningTrace;
    }

    public void setReasoningTrace(List<String> reasoningTrace) {
        this.reasoningTrace = reasoningTrace;
    }

    public String getCalorieStatusReason() {
        return calorieStatusReason;
    }

    public void setCalorieStatusReason(String calorieStatusReason) {
        this.calorieStatusReason = calorieStatusReason;
    }

    public String getProteinStatusReason() {
        return proteinStatusReason;
    }

    public void setProteinStatusReason(String proteinStatusReason) {
        this.proteinStatusReason = proteinStatusReason;
    }

    public String getFiberStatusReason() {
        return fiberStatusReason;
    }

    public void setFiberStatusReason(String fiberStatusReason) {
        this.fiberStatusReason = fiberStatusReason;
    }

    public double getCaloriePercentage() {
        return caloriePercentage;
    }

    public void setCaloriePercentage(double caloriePercentage) {
        this.caloriePercentage = caloriePercentage;
    }

    public double getProteinPercentage() {
        return proteinPercentage;
    }

    public void setProteinPercentage(double proteinPercentage) {
        this.proteinPercentage = proteinPercentage;
    }

    public double getFiberPercentage() {
        return fiberPercentage;
    }

    public void setFiberPercentage(double fiberPercentage) {
        this.fiberPercentage = fiberPercentage;
    }

    private java.util.List<String> analyticSeverities = new java.util.ArrayList<>();

    public java.util.List<String> getAnalyticSeverities() {
        return analyticSeverities;
    }

    public void setAnalyticSeverities(java.util.List<String> analyticSeverities) {
        this.analyticSeverities = analyticSeverities;
    }

    private java.util.List<String> firedRuleNames = new java.util.ArrayList<>();

    public java.util.List<String> getFiredRuleNames() {
        return firedRuleNames;
    }

    public void setFiredRuleNames(java.util.List<String> firedRuleNames) {
        this.firedRuleNames = firedRuleNames;
    }
}