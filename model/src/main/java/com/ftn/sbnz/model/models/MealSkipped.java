package com.ftn.sbnz.model.models;

import com.ftn.sbnz.model.enums.MealCategory;
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;

import java.time.LocalDate;

@Role(Role.Type.EVENT)
@Expires("30d")
public class MealSkipped {

    private Long userId;
    private MealCategory category;
    private LocalDate date;

    public MealSkipped() {
    }

    public MealSkipped(Long userId, MealCategory category, LocalDate date) {
        this.userId = userId;
        this.category = category;
        this.date = date;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public MealCategory getCategory() {
        return category;
    }

    public void setCategory(MealCategory category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
