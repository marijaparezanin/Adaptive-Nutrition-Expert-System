package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.model.models.AnalyticStatus;
import com.ftn.sbnz.model.models.BingePattern;
import com.ftn.sbnz.model.models.BreakfastSkippingPattern;
import com.ftn.sbnz.model.models.ConsistentProteinDeficit;
import com.ftn.sbnz.model.models.DailyIntake;
import com.ftn.sbnz.model.models.LateNightEatingPattern;
import com.ftn.sbnz.model.models.LongFastDetected;
import com.ftn.sbnz.model.models.MissedMealAlert;
import com.ftn.sbnz.model.models.Recommendation;
import com.ftn.sbnz.model.models.UnplannedSnackingPattern;
import com.ftn.sbnz.model.models.User;
import com.ftn.sbnz.model.models.WeightStagnationDetected;
import com.ftn.sbnz.model.enums.AnalyticStatusType;
import com.ftn.sbnz.model.enums.SeverityLevel;
import com.ftn.sbnz.service.dto.NutritionEvaluationRequest;
import com.ftn.sbnz.service.dto.NutritionEvaluationResponse;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nutrition")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class NutritionEvaluationController {

    private final KieContainer kieContainer;

    public NutritionEvaluationController(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @PostMapping("/evaluate")
    public NutritionEvaluationResponse evaluate(@RequestBody NutritionEvaluationRequest request) {
        // Use named session to match kmodule.xml definition
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            if (request == null) {
                request = new NutritionEvaluationRequest();
            }

            // Track which rules fired by name
            List<String> firedRuleNames = new ArrayList<>();
            session.addEventListener(new DefaultAgendaEventListener() {
                @Override
                public void afterMatchFired(AfterMatchFiredEvent event) {
                    firedRuleNames.add(event.getMatch().getRule().getName());
                }
            });

            User user = request.getUser();
            DailyIntake dailyIntake = request.getDailyIntake();

            if (user != null) session.insert(user);
            if (dailyIntake != null) session.insert(dailyIntake);
            if (request.getWeeklyPattern() != null) session.insert(request.getWeeklyPattern());
            insertAll(session, request.getMeals());
            insertAll(session, request.getPreviousDailyIntakes());
            insertAll(session, request.getSkippedMeals());
            insertAll(session, request.getWeightMeasurements());
            insertAll(session, request.getActivityChanges());
            insertAll(session, request.getMissedMealAlerts());

            int firedRules = session.fireAllRules();

            NutritionEvaluationResponse response = new NutritionEvaluationResponse();
            response.setFiredRules(firedRules);
            response.setFiredRuleNames(firedRuleNames);

            if (user != null) {
                response.setTargetCalories(user.getTargetCalories());
                response.setTargetProtein(user.getTargetProtein());
                response.setTargetFiber(user.getTargetFiber());
                response.setBmr(user.getBmr());
            }

            if (dailyIntake != null && dailyIntake.getStatus() != null) {
                response.setDailyStatus(dailyIntake.getStatus().name());
            }

            if (dailyIntake != null) {
                response.setTotalCalories(dailyIntake.getTotalCalories());
                response.setTotalProtein(dailyIntake.getTotalProtein());
                response.setTotalFiber(dailyIntake.getTotalFiber());
                response.setMealCount(dailyIntake.getMealCount());

                if (user != null && user.getTargetCalories() > 0) {
                    response.setCaloriesRemaining(user.getTargetCalories() - dailyIntake.getTotalCalories());
                    response.setProteinRemaining(user.getTargetProtein() - dailyIntake.getTotalProtein());
                    response.setFiberRemaining(user.getTargetFiber() - dailyIntake.getTotalFiber());
                    response.setCaloriePercentage((dailyIntake.getTotalCalories() / user.getTargetCalories()) * 100);
                    if (user.getTargetProtein() > 0)
                        response.setProteinPercentage((dailyIntake.getTotalProtein() / user.getTargetProtein()) * 100);
                    if (user.getTargetFiber() > 0)
                        response.setFiberPercentage((dailyIntake.getTotalFiber() / user.getTargetFiber()) * 100);
                }
            }

            // Collect analytic statuses sorted by severity (CRITICAL first)
            List<AnalyticStatus> analyticStatusObjects = session.getObjects(o -> o instanceof AnalyticStatus)
                .stream()
                .map(o -> (AnalyticStatus) o)
                .sorted((a, b) -> severityOrder(b.getSeverity()) - severityOrder(a.getSeverity()))
                .collect(Collectors.toList());

            response.setAnalyticStatuses(analyticStatusObjects.stream()
                .map(o -> o.getType().name())
                .collect(Collectors.toList()));

            response.setAnalyticSeverities(analyticStatusObjects.stream()
                .map(o -> o.getSeverity() != null ? o.getSeverity().name() : "WARNING")
                .collect(Collectors.toList()));

            // Collect recommendations
            List<Recommendation> recommendationObjects = session.getObjects(o -> o instanceof Recommendation)
                .stream()
                .map(o -> (Recommendation) o)
                .collect(Collectors.toList());

            response.setRecommendations(recommendationObjects.stream()
                .map(Recommendation::getMessage)
                .collect(Collectors.toList()));
            response.setRecommendationTypes(recommendationObjects.stream()
                .map(Recommendation::getType)
                .collect(Collectors.toList()));

            // Collect patterns
            Collection<Object> patternObjects = new java.util.ArrayList<>(session.getObjects(this::isPattern));
            response.setDetectedPatterns(patternObjects.stream()
                .map(o -> o.getClass().getSimpleName())
                .sorted()
                .collect(Collectors.toList()));
            response.setPatternDetails(buildPatternDetails(patternObjects));

            // Build rich reasoning and expert opinion
            response.setReasoningTrace(buildReasoningTrace(response, dailyIntake, user));
            response.setExpertOpinionSummary(buildExpertOpinionSummary(response, analyticStatusObjects));
            response.setCalorieStatusReason(buildCalorieStatusReason(response, user));
            response.setProteinStatusReason(buildProteinStatusReason(response, user));
            response.setFiberStatusReason(buildFiberStatusReason(response, user));

            return response;
        } finally {
            session.dispose();
        }
    }

    private int severityOrder(SeverityLevel level) {
        if (level == null) return 0;
        switch (level) {
            case CRITICAL: return 3;
            case HIGH: return 2;
            case WARNING: return 1;
            case LOW: return 0;
            default: return 0;
        }
    }

    private boolean isPattern(Object object) {
        return object instanceof BreakfastSkippingPattern
            || object instanceof LateNightEatingPattern
            || object instanceof BingePattern
            || object instanceof UnplannedSnackingPattern
            || object instanceof ConsistentProteinDeficit
            || object instanceof WeightStagnationDetected
            || object instanceof LongFastDetected
            || object instanceof MissedMealAlert;
    }

    private void insertAll(KieSession session, Collection<?> facts) {
        if (facts == null) return;
        facts.stream().filter(f -> f != null).forEach(session::insert);
    }

    private List<String> buildReasoningTrace(NutritionEvaluationResponse response, DailyIntake dailyIntake, User user) {
        List<String> trace = new ArrayList<>();
        if (user != null) {
            trace.add(String.format("BMR: %.0f kcal (Mifflin-St Jeor)", response.getBmr()));
            trace.add(String.format("Daily target: %.0f kcal (BMR × %.2f activity, goal-adjusted)", response.getTargetCalories(), user.getActivityFactor()));
            trace.add(String.format("Protein target: %.0f g/day (%.1f g/kg)", response.getTargetProtein(), user.getTargetProtein() / Math.max(1, user.getWeight())));
            trace.add(String.format("Fiber target: %.0f g/day", response.getTargetFiber()));
        }
        if (dailyIntake != null) {
            trace.add(String.format("Meals logged: %d", response.getMealCount()));
            trace.add(String.format("Calories: %.0f kcal → %.0f%% of target", response.getTotalCalories(), response.getCaloriePercentage()));
            trace.add(String.format("Protein: %.0f g → %.0f%% of target", response.getTotalProtein(), response.getProteinPercentage()));
            trace.add(String.format("Fiber: %.0f g → %.0f%% of target", response.getTotalFiber(), response.getFiberPercentage()));
        }
        trace.add(String.format("Drools fired %d rules → %d analytic signal%s, %d recommendation%s",
            response.getFiredRules(),
            response.getAnalyticStatuses().size(), response.getAnalyticStatuses().size() == 1 ? "" : "s",
            response.getRecommendations().size(), response.getRecommendations().size() == 1 ? "" : "s"));
        if (!response.getDetectedPatterns().isEmpty()) {
            trace.add("CEP patterns: " + String.join(", ", response.getDetectedPatterns()));
        }
        return trace;
    }

    private String buildExpertOpinionSummary(NutritionEvaluationResponse response, List<AnalyticStatus> statuses) {
        StringBuilder sb = new StringBuilder();

        boolean hasCritical = statuses.stream().anyMatch(s -> s.getSeverity() == SeverityLevel.CRITICAL);
        boolean hasHigh = statuses.stream().anyMatch(s -> s.getSeverity() == SeverityLevel.HIGH);

        boolean hasPlanChange = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.PLAN_CHANGE_NEEDED);
        boolean hasAdaptation = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.CALORIC_ADAPTATION_BLOCK);
        boolean hasProteinCritical = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.PROTEIN_CRITICAL);
        boolean hasMuscleLoss = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.MUSCLE_LOSS_RISK);
        boolean hasOvereating = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.OVEREATING_DETECTED);
        boolean hasSurplus = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.CALORIC_SURPLUS_ACCUMULATION);
        boolean hasCircadian = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.CIRCADIAN_RHYTHM_DISRUPTION);
        boolean hasEnergyShortage = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.ENERGY_SHORTAGE);
        boolean hasStagnation = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.WEIGHT_STAGNATION_CONFIRMED);
        boolean hasExtremeRestriction = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.EXTREME_CALORIC_RESTRICTION);
        boolean hasLinkedPattern = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.LINKED_PATTERN_STAGNATION_RISK);
        boolean hasReactiveEating = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.REACTIVE_EATING_CONFIRMED);
        boolean hasGoalConflictActivity = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.GOAL_CONFLICT_ACTIVITY_NEEDED);
        boolean hasWeeklyProteinDeficit = statuses.stream().anyMatch(s -> s.getType() == AnalyticStatusType.WEEKLY_PROTEIN_DEFICIT);

        if (hasPlanChange || hasAdaptation) {
            sb.append("CRITICAL — Metabolic adaptation detected. Your body has adjusted to severe caloric restriction and weight loss has stalled. Further restriction is counterproductive. A structured plan recalibration (diet break, refeed, or calorie cycling) is strongly recommended.");
        } else if (hasProteinCritical || hasMuscleLoss) {
            sb.append("CRITICAL — Chronic protein deficit with negative weight trend detected. The body is catabolizing lean tissue during this deficit. Immediate protein increase is required to prevent muscle loss.");
        } else if (hasExtremeRestriction) {
            sb.append("CRITICAL — Weekly calorie average is below 50% of BMR. This level of restriction causes hormonal disruption, muscle loss, and metabolic slowdown. It is not safe to sustain.");
        } else if (hasLinkedPattern) {
            sb.append("CRITICAL — Compound pattern linkage confirmed by expert system: breakfast skipping + late-night eating + weight stagnation are operating as a reinforcing cycle. This three-way pattern is the primary driver of your stagnated progress.");
        } else if (hasReactiveEating) {
            sb.append("HIGH — Reactive eating pattern confirmed: daytime energy restriction is directly causing late-night caloric compensation. The expert system traced the causal chain through ENERGY_SHORTAGE → CIRCADIAN_DISRUPTION → late-night intake.");
        } else if (hasGoalConflictActivity) {
            sb.append("HIGH — Goal conflict: sustained caloric surplus detected while pursuing weight loss. The expert system recommends adding physical activity or reducing daily intake by 300-400 kcal to restore the required deficit.");
        } else if (hasWeeklyProteinDeficit) {
            sb.append("HIGH — Chronic weekly protein deficit confirmed. Protein intake has been below 80% of target across the entire week. Muscle protein synthesis is compromised — restructure meals to prioritize protein at each sitting.");
        } else if (hasOvereating && hasEnergyShortage) {
            sb.append("HIGH ALERT — The expert system traced a meal-skipping → compensatory overeating chain. Skipped meals caused energy depletion (ENERGY_SHORTAGE), which triggered a large reactive meal. This pattern is self-reinforcing and undermines your goal. Regular meal timing is key.");
        } else if (hasSurplus && hasCritical) {
            sb.append("HIGH — Caloric surplus is accumulating and conflicts with your current goal. Review portion sizes and meal composition for the remainder of the day.");
        } else if (hasCircadian) {
            sb.append("WARNING — Late-night caloric loading detected. Consuming >30% of daily calories after 22:00 disrupts circadian rhythm, degrades sleep quality, and promotes adipose storage over metabolic use.");
        } else if (hasStagnation) {
            sb.append("WARNING — Weight stagnation detected while in a caloric deficit. Metabolic adaptation is likely. Consider a structured refeed day or adjusting your calorie cycling strategy.");
        } else if (hasSurplus) {
            sb.append("WARNING — Today's intake exceeds your daily target. Adjust remaining meals accordingly.");
        } else if (hasHigh) {
            sb.append("Nutritional imbalances flagged. The expert system has generated signals that require attention — see analytic statuses and recommendations below.");
        } else if (statuses.isEmpty()) {
            sb.append("No active expert signals. Your nutrition data for this day appears balanced. Continue logging consistently to enable multi-day pattern detection.");
        } else {
            sb.append("Monitoring active. Low-priority nutrition signals present — keep logging to enable deeper pattern analysis over the coming days.");
        }

        // Caloric context
        if (response.getDailyStatus() != null) {
            switch (response.getDailyStatus()) {
                case "SURPLUS": sb.append(" | Daily balance: SURPLUS"); break;
                case "DEFICIT": sb.append(" | Daily balance: DEFICIT (supports fat loss)"); break;
                case "MAINTENANCE_STABLE": sb.append(" | Daily balance: MAINTENANCE"); break;
            }
        }

        if (!response.getDetectedPatterns().isEmpty()) {
            sb.append(String.format(" | CEP detected %d pattern(s): %s",
                response.getDetectedPatterns().size(),
                String.join(", ", response.getDetectedPatterns())));
        }

        return sb.toString();
    }

    private String buildCalorieStatusReason(NutritionEvaluationResponse response, User user) {
        if (user == null || response.getTargetCalories() <= 0)
            return "Configure your profile in Goals to enable calorie tracking.";
        double pct = response.getCaloriePercentage();
        double remaining = response.getCaloriesRemaining();
        if (pct == 0) return "No meals logged yet. Daily target: " + Math.round(response.getTargetCalories()) + " kcal.";
        if (pct < 50) return String.format("Very low intake: %.0f%% of target (%.0f kcal consumed, %.0f kcal remaining). Risk of extreme restriction.", pct, response.getTotalCalories(), remaining);
        if (pct < 70) return String.format("Under-eating: %.0f%% of target. %.0f kcal remaining — add a balanced meal.", pct, remaining);
        if (pct <= 110) return String.format("On track: %.0f%% of daily target. %.0f kcal remaining.", pct, Math.max(0, remaining));
        if (pct <= 130) return String.format("Slightly over: %.0f%% of target (%.0f kcal over goal). Plan lighter remaining meals.", pct, Math.abs(remaining));
        return String.format("Significantly over: %.0f%% of target (%.0f kcal over). This conflicts with your %s goal.", pct, Math.abs(remaining),
            user.getGoal() != null ? user.getGoal().name().toLowerCase().replace('_', ' ') : "nutrition");
    }

    private String buildProteinStatusReason(NutritionEvaluationResponse response, User user) {
        if (user == null || response.getTargetProtein() <= 0)
            return "Set your profile to enable protein tracking.";
        double pct = response.getProteinPercentage();
        double remaining = response.getProteinRemaining();
        if (pct == 0) return "No protein logged. Target: " + Math.round(response.getTargetProtein()) + "g/day.";
        if (pct < 50) return String.format("Critical protein gap: %.0f%% of target (%.0f g consumed, %.0f g remaining). Muscle preservation at risk.", pct, response.getTotalProtein(), remaining);
        if (pct < 80) return String.format("Protein deficit: %.0f%% of target. Add lean meat, eggs, dairy, or legumes — %.0f g still needed.", pct, remaining);
        if (pct <= 120) return String.format("Good protein intake: %.0f%% of target. %.0f g remaining.", pct, Math.max(0, remaining));
        return String.format("High protein intake: %.0f%% of target. Ensure adequate hydration.", pct);
    }

    private String buildFiberStatusReason(NutritionEvaluationResponse response, User user) {
        if (user == null || response.getTargetFiber() <= 0)
            return "Set your profile to enable fiber tracking.";
        double pct = response.getFiberPercentage();
        double remaining = response.getFiberRemaining();
        if (pct == 0) return "No fiber logged. Target: " + Math.round(response.getTargetFiber()) + "g/day. Add vegetables, legumes, whole grains.";
        if (pct < 60) return String.format("Low fiber: %.0f%% of target (%.0f g remaining). Prioritize vegetables and whole grains.", pct, remaining);
        if (pct < 90) return String.format("Moderate fiber: %.0f%% of target. %.0f g more from plant foods recommended.", pct, remaining);
        if (pct <= 120) return String.format("Good fiber intake: %.0f%% of target — supporting digestive health.", pct);
        return String.format("Excellent fiber: %.0f%% of target. Maintain good hydration.", pct);
    }

    private List<String> buildPatternDetails(Collection<Object> patterns) {
        List<String> details = new ArrayList<>();
        for (Object pattern : patterns) {
            String detail = buildDetailForPattern(pattern);
            if (detail != null && !detail.isEmpty()) details.add(detail);
        }
        return details;
    }

    private String buildDetailForPattern(Object pattern) {
        if (pattern instanceof LateNightEatingPattern) {
            LateNightEatingPattern p = (LateNightEatingPattern) pattern;
            return "Late Night Eating (" + p.getLateMealCount() + " occurrences): Meals after 22:00 disrupt circadian rhythm, impair sleep-related fat metabolism, and increase cortisol. Move your last meal before 20:00.";
        }
        if (pattern instanceof BreakfastSkippingPattern) {
            BreakfastSkippingPattern p = (BreakfastSkippingPattern) pattern;
            return "Breakfast Skipping (" + p.getSkippedDays() + " days): Consistently skipping breakfast suppresses morning metabolism and leads to larger compensatory meals later. A 250–350 kcal breakfast by 09:00 stabilizes blood glucose.";
        }
        if (pattern instanceof BingePattern) {
            BingePattern p = (BingePattern) pattern;
            return "Rapid Meal Cluster (" + p.getMealsInWindow() + " meals in " + p.getWindowMinutes() + " min): Multiple eating events in a short window override satiety signaling before fullness registers. Wait 20+ minutes between eating episodes.";
        }
        if (pattern instanceof UnplannedSnackingPattern) {
            UnplannedSnackingPattern p = (UnplannedSnackingPattern) pattern;
            return "Unplanned Snacking (" + p.getSnackCount() + " snacks): More than 3 unplanned snacks suggests reactive eating driven by habit or unchecked hunger. Pre-plan snacks with protein and fiber to reduce impulsive grazing.";
        }
        if (pattern instanceof ConsistentProteinDeficit) {
            ConsistentProteinDeficit p = (ConsistentProteinDeficit) pattern;
            return "Chronic Protein Deficit (" + p.getDaysBelowTarget() + " days below 70% of target): Sustained low protein during a caloric deficit accelerates lean muscle loss. The body breaks down muscle for energy when protein is insufficient. Prioritize protein at every meal.";
        }
        if (pattern instanceof WeightStagnationDetected) {
            WeightStagnationDetected p = (WeightStagnationDetected) pattern;
            return String.format("Weight Stagnation (%.2f kg variance): Weight has barely changed over the tracking period. If fat loss is the goal, the body has adapted — consider a structured refeed day or calorie cycling rather than further restriction.", p.getVarianceKg());
        }
        if (pattern instanceof LongFastDetected) {
            LongFastDetected p = (LongFastDetected) pattern;
            return String.format("Extended Fasting (%.0f h gap): A gap exceeding 6 hours during active hours can elevate cortisol, increase catabolism, and trigger rebound hunger. Unless following intentional IF, schedule a small protein-rich snack to bridge the gap.", p.getHoursWithoutMeal());
        }
        if (pattern instanceof MissedMealAlert) {
            MissedMealAlert p = (MissedMealAlert) pattern;
            return String.format("Missed Meal — %s (%dh past expected time): The pseudo-clock simulation flagged that no %s was logged by end of day. This pattern, when recurring, significantly disrupts daily energy distribution.", p.getMissedCategory(), p.getHoursPastExpected(), p.getMissedCategory().toLowerCase());
        }
        return "";
    }
}
