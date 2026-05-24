package com.ftn.sbnz.model.models;

import java.time.LocalDate;

import com.ftn.sbnz.model.enums.DailyIntakeStatus;
import com.ftn.sbnz.model.models.Meal;
public class DailyIntake {

    private LocalDate date;
    private double totalCalories;
    private double averageCalories;
    private double totalProtein;
    private double totalFiber;
    private int mealCount;
    private DailyIntakeStatus status;
    private Long userId;

    public DailyIntake() {
    }

    public DailyIntake(LocalDate date,
                       double totalCalories,
                       double totalProtein,
                       double totalFiber,
                       int mealCount,
                       DailyIntakeStatus status,
                       Long userId) {
        this.date = date;
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.totalFiber = totalFiber;
        this.mealCount = mealCount;
        this.status = status;
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
        this.averageCalories = totalCalories;
    }

    public double getAverageCalories() {
        return averageCalories;
    }

    public void setAverageCalories(double averageCalories) {
        this.averageCalories = averageCalories;
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


    public int getMealCount() {
        return mealCount;
    }

    public void setMealCount(int mealCount) {
        this.mealCount = mealCount;
    }

    public DailyIntakeStatus getStatus() {
        return status;
    }

    public void setStatus(DailyIntakeStatus status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void addMeal(Meal meal) {
        this.totalCalories += meal.getCalories();
        this.averageCalories = this.totalCalories;
        this.totalProtein += meal.getProtein();
        this.totalFiber += meal.getFiber();
        this.mealCount++;
    }
}
