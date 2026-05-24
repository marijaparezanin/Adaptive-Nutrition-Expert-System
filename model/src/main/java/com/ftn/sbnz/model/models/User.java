package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.Gender;
import com.ftn.sbnz.model.enums.GoalType;

import java.util.ArrayList;
import java.util.List;

public class User {

    private Long id;
    private Gender gender;
    private int age;
    private double weight;
    private double height;
    private GoalType goal;
    private double activityFactor;
    private List<String> allergies = new ArrayList<>();
    private double targetCalories;
    private double targetProtein;
    private double targetFiber;
    private double bmr;

    public User() {
    }

    public User(Long id,
                Gender gender,
                int age,
                double weight,
                double height,
                GoalType goal,
                double activityFactor,
                double targetCalories,
                double targetProtein,
                double targetFiber
                ) {
        this.id = id;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.goal = goal;
        this.activityFactor = activityFactor;
        this.targetCalories = targetCalories;
        this.targetProtein = targetProtein;
        this.targetFiber = targetFiber;
        this.bmr = calculateBMR();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public GoalType getGoal() {
        return goal;
    }

    public void setGoal(GoalType goal) {
        this.goal = goal;
    }

    public double getActivityFactor() {
        return activityFactor;
    }

    public void setActivityFactor(double activityFactor) {
        this.activityFactor = activityFactor;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
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

    public double getBmr() {
        return bmr;
    }

    public void setBmr(double bmr) {
        this.bmr = bmr;
    }

    public void initializeTargets() {
        this.bmr = calculateBMR();
        this.targetCalories = calculateTDEE();
        if (goal == GoalType.MUSCLE_GAIN) {
            this.targetProtein = weight * 1.8;
        } else if (goal == GoalType.WEIGHT_LOSS) {
            this.targetProtein = weight * 1.4;
        } else {
            this.targetProtein = weight * 1.0;
        }
        this.targetFiber = gender == Gender.FEMALE ? 25 : 38;
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
}
