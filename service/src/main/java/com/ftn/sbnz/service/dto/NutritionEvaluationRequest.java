package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.models.DailyIntake;
import com.ftn.sbnz.model.models.Meal;
import com.ftn.sbnz.model.events.MealSkipped;
import com.ftn.sbnz.model.events.NewActivityRoutine;
import com.ftn.sbnz.model.events.WeightMeasured;
import com.ftn.sbnz.model.models.User;
import com.ftn.sbnz.model.models.WeeklyPattern;

import java.util.ArrayList;
import java.util.List;

public class NutritionEvaluationRequest {

    private User user;
    private DailyIntake dailyIntake;
    private WeeklyPattern weeklyPattern;
    private List<Meal> meals = new ArrayList<>();
    private List<DailyIntake> previousDailyIntakes = new ArrayList<>();
    private List<MealSkipped> skippedMeals = new ArrayList<>();
    private List<WeightMeasured> weightMeasurements = new ArrayList<>();
    private List<NewActivityRoutine> activityChanges = new ArrayList<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DailyIntake getDailyIntake() {
        return dailyIntake;
    }

    public void setDailyIntake(DailyIntake dailyIntake) {
        this.dailyIntake = dailyIntake;
    }

    public WeeklyPattern getWeeklyPattern() {
        return weeklyPattern;
    }

    public void setWeeklyPattern(WeeklyPattern weeklyPattern) {
        this.weeklyPattern = weeklyPattern;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

    public List<DailyIntake> getPreviousDailyIntakes() {
        return previousDailyIntakes;
    }

    public void setPreviousDailyIntakes(List<DailyIntake> previousDailyIntakes) {
        this.previousDailyIntakes = previousDailyIntakes;
    }

    public List<MealSkipped> getSkippedMeals() {
        return skippedMeals;
    }

    public void setSkippedMeals(List<MealSkipped> skippedMeals) {
        this.skippedMeals = skippedMeals;
    }

    public List<WeightMeasured> getWeightMeasurements() {
        return weightMeasurements;
    }

    public void setWeightMeasurements(List<WeightMeasured> weightMeasurements) {
        this.weightMeasurements = weightMeasurements;
    }

    public List<NewActivityRoutine> getActivityChanges() {
        return activityChanges;
    }

    public void setActivityChanges(List<NewActivityRoutine> activityChanges) {
        this.activityChanges = activityChanges;
    }
}
