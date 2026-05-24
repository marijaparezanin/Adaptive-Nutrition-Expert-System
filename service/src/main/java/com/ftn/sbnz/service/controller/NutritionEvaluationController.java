package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.model.models.AnalyticStatus;
import com.ftn.sbnz.model.models.BingePattern;
import com.ftn.sbnz.model.models.BreakfastSkippingPattern;
import com.ftn.sbnz.model.models.ConsistentProteinDeficit;
import com.ftn.sbnz.model.models.DailyIntake;
import com.ftn.sbnz.model.models.LateNightEatingPattern;
import com.ftn.sbnz.model.models.LongFastDetected;
import com.ftn.sbnz.model.models.Recommendation;
import com.ftn.sbnz.model.models.UnplannedSnackingPattern;
import com.ftn.sbnz.model.models.User;
import com.ftn.sbnz.model.models.WeightStagnationDetected;
import com.ftn.sbnz.service.dto.NutritionEvaluationRequest;
import com.ftn.sbnz.service.dto.NutritionEvaluationResponse;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionEvaluationController {

    private final KieContainer kieContainer;

    public NutritionEvaluationController(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @PostMapping("/evaluate")
    public NutritionEvaluationResponse evaluate(@RequestBody NutritionEvaluationRequest request) {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            if (request == null) {
                request = new NutritionEvaluationRequest();
            }

            User user = request.getUser();
            DailyIntake dailyIntake = request.getDailyIntake();

            if (user != null) {
                session.insert(user);
            }
            if (dailyIntake != null) {
                session.insert(dailyIntake);
            }
            if (request.getWeeklyPattern() != null) {
                session.insert(request.getWeeklyPattern());
            }
            insertAll(session, request.getMeals());
            insertAll(session, request.getPreviousDailyIntakes());
            insertAll(session, request.getSkippedMeals());
            insertAll(session, request.getWeightMeasurements());
            insertAll(session, request.getActivityChanges());

            int firedRules = session.fireAllRules();

            NutritionEvaluationResponse response = new NutritionEvaluationResponse();
            response.setFiredRules(firedRules);
            if (user != null) {
                response.setTargetCalories(user.getTargetCalories());
                response.setTargetProtein(user.getTargetProtein());
            }
            if (dailyIntake != null && dailyIntake.getStatus() != null) {
                response.setDailyStatus(dailyIntake.getStatus().name());
            }
            response.setAnalyticStatuses(session.getObjects(o -> o instanceof AnalyticStatus)
                .stream()
                .map(o -> ((AnalyticStatus) o).getType().name())
                .sorted()
                .collect(Collectors.toList()));
            response.setRecommendations(session.getObjects(o -> o instanceof Recommendation)
                .stream()
                .map(o -> ((Recommendation) o).getMessage())
                .sorted()
                .collect(Collectors.toList()));
            response.setDetectedPatterns(session.getObjects(this::isPattern)
                .stream()
                .map(o -> o.getClass().getSimpleName())
                .sorted()
                .collect(Collectors.toList()));
            return response;
        } finally {
            session.dispose();
        }
    }

    private boolean isPattern(Object object) {
        return object instanceof BreakfastSkippingPattern ||
            object instanceof LateNightEatingPattern ||
            object instanceof BingePattern ||
            object instanceof UnplannedSnackingPattern ||
            object instanceof ConsistentProteinDeficit ||
            object instanceof WeightStagnationDetected ||
            object instanceof LongFastDetected;
    }

    private void insertAll(KieSession session, Collection<?> facts) {
        if (facts == null) {
            return;
        }
        facts.stream()
            .filter(fact -> fact != null)
            .forEach(session::insert);
    }
}
