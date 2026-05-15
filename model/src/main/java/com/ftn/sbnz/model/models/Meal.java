package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.MealCategory;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.time.LocalDateTime;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
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

    // getters and setters
}