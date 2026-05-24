package com.ftn.sbnz.model.events;

import com.ftn.sbnz.model.enums.MealCategory;
import com.ftn.sbnz.model.models.Meal;
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.time.LocalDateTime;

@Role(Role.Type.EVENT)
@Timestamp("timestampMillis")
@Expires("30d")
public class MealLogged extends Meal {

    public MealLogged() {
    }

    public MealLogged(String name,
                      MealCategory category,
                      double calories,
                      double protein,
                      double carbohydrates,
                      double fat,
                      double fiber,
                      double sugars,
                      LocalDateTime timestamp,
                      Long userId) {
        super(name, category, calories, protein, carbohydrates, fat, fiber, sugars, timestamp, userId);
    }
}
