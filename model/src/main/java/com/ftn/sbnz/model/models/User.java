package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.Gender;
import com.ftn.sbnz.model.enums.GoalType;

import java.util.List;

public class User {

    private Long id;

    private Gender gender;

    private int age;

    private double weight;

    private double height;

    private GoalType goal;

    private double activityFactor;

    private List<String> allergies;

    private double targetCalories;

    private double targetProtein;

    private double targetFiber;

    public User() {
    }

    public User(Long id, Gender gender, int age, double weight, double height,
                GoalType goal, double activityFactor, List<String> allergies,
                double targetCalories, double targetProtein, double targetFiber) {

        this.id = id;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.goal = goal;
        this.activityFactor = activityFactor;
        this.allergies = allergies;
        this.targetCalories = targetCalories;
        this.targetProtein = targetProtein;
        this.targetFiber = targetFiber;
    }

    public double calculateBMR() {

        if (gender == Gender.MALE) {
            return (10 * weight) + (6.25 * height) - (5 * age) + 5;
        }

        return (10 * weight) + (6.25 * height) - (5 * age) - 161;
    }

    public double calculateTDEE() {

        double bmr = calculateBMR();

        double adjustment = 0;

        switch (goal) {

            case WEIGHT_LOSS:
                adjustment = -500;
                break;

            case MUSCLE_GAIN:
                adjustment = 300;
                break;

            case MAINTENANCE:
                adjustment = 0;
                break;
        }

        return (bmr * activityFactor) + adjustment;
    }

    // getters and setters
}