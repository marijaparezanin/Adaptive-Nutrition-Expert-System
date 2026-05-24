package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.MealCategory;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Role(Role.Type.EVENT)
@Timestamp("timestampMillis")
@Expires("30d")
public class Meal {

    private String name;
    private MealCategory category;
    private double calories;
    private double protein;
    private double carbohydrates;
    private double fat;
    private double fiber;
    private double sugars;
    private LocalDateTime timestamp;
    private Long userId;
    private boolean processed;

    public Meal() {
    }

    public Meal(String name,
                MealCategory category,
                double calories,
                double protein,
                double carbohydrates,
                double fat,
                double fiber,
                double sugars,
                LocalDateTime timestamp,
                Long userId) {
        this.name = name;
        this.category = category;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.fiber = fiber;
        this.sugars = sugars;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MealCategory getCategory() {
        return category;
    }

    public void setCategory(MealCategory category) {
        this.category = category;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getFiber() {
        return fiber;
    }

    public void setFiber(double fiber) {
        this.fiber = fiber;
    }

    public double getSugars() {
        return sugars;
    }

    public void setSugars(double sugars) {
        this.sugars = sugars;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestampMillis() {
        if (timestamp == null) {
            return System.currentTimeMillis();
        }
        return timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
