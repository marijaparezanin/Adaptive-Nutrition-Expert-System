package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.model.enums.AnalyticStatusType;
import com.ftn.sbnz.model.enums.DailyIntakeStatus;
import com.ftn.sbnz.model.enums.Gender;
import com.ftn.sbnz.model.enums.GoalType;
import com.ftn.sbnz.model.enums.MealCategory;
import com.ftn.sbnz.model.enums.WeightTrend;
import com.ftn.sbnz.model.events.MealLogged;
import com.ftn.sbnz.model.events.MealSkipped;
import com.ftn.sbnz.model.events.NewActivityRoutine;
import com.ftn.sbnz.model.events.WeightMeasured;
import com.ftn.sbnz.model.models.AnalyticStatus;
import com.ftn.sbnz.model.models.BingePattern;
import com.ftn.sbnz.model.models.BreakfastSkippingPattern;
import com.ftn.sbnz.model.models.ConsistentProteinDeficit;
import com.ftn.sbnz.model.models.DailyIntake;
import com.ftn.sbnz.model.models.LateNightEatingPattern;
import com.ftn.sbnz.model.models.LongFastDetected;
import com.ftn.sbnz.model.models.Meal;
import com.ftn.sbnz.model.models.NutritionPattern;
import com.ftn.sbnz.model.models.Recommendation;
import com.ftn.sbnz.model.models.RuleTemplateParameter;
import com.ftn.sbnz.model.models.UnplannedSnackingPattern;
import com.ftn.sbnz.model.models.User;
import com.ftn.sbnz.model.models.WeeklyPattern;
import com.ftn.sbnz.model.models.WeightStagnationDetected;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/demo")
public class NutritionDemoController {

    private final KieContainer kieContainer;

    public NutritionDemoController(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @GetMapping("/rules")
    public Map<String, Object> runAllDemo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("forward", runForwardDemo());
        result.put("cep", runCepDemo());
        result.put("weeklyLogging", runWeeklyLoggingDemo());
        result.put("bingeStory", runBingeStoryDemo());
        result.put("skippedBreakfastOvereating", runSkippedBreakfastOvereatingStory());
        result.put("proteinCriticalStory", runProteinCriticalStory());
        result.put("metabolicAdaptationStory", runMetabolicAdaptationStory());
        result.put("templates", runTemplateDemo());
        result.put("calorieDeficit", runCalorieDeficitDemo());
        result.put("maintenance", runMaintenanceDemo());
        result.put("massGainConflict", runMassGainConflictDemo());
        result.put("proteinDistribution", runProteinDistributionDemo());
        result.put("highProteinHydration", runHighProteinHydrationDemo());
        result.put("longFast", runLongFastDemo());
        result.put("activityAndWeight", runActivityAndWeightDemo());
        result.put("backwardOvereating", runBackwardOvereatingDemo());
        result.put("backwardProtein", runBackwardProteinDemo());
        result.put("backwardPlan", runBackwardPlanDemo());
        return result;
    }

    @GetMapping("/forward")
    public Map<String, Object> runForwardDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 1L;
            LocalDate today = LocalDate.now().minusDays(1);
            User user = weightLossUser(userId);
            DailyIntake todayIntake = new DailyIntake(today, 0, 0, 0, 0, null, userId);
            List<Meal> loggedMeals = java.util.Arrays.asList(
                meal("Ovsena kasa sa jogurtom", MealCategory.BREAKFAST, 420, 24, today.atTime(8, 10), userId),
                meal("Piletina sa rizom", MealCategory.LUNCH, 620, 42, today.atTime(13, 20), userId),
                meal("Cokoladica posle posla", MealCategory.SNACK, 310, 5, today.atTime(17, 45), userId),
                meal("Kasna dostava burger i pomfrit", MealCategory.DINNER, 1150, 18, today.atTime(22, 40), userId)
            );

            session.insert(user);
            session.insert(todayIntake);
            loggedMeals.forEach(session::insert);

            int firedRules = session.fireAllRules();
            Map<String, Object> result = baseResult("forward-chaining", firedRules, user, todayIntake, session);
            result.put("scenario", "User logs a full day of meals; Drools aggregates the day and derives statuses.");
            result.put("loggedMeals", mealLog(loggedMeals));
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/cep")
    public Map<String, Object> runCepDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 2L;
            LocalDate today = LocalDate.now().minusDays(1);
            User user = weightLossUser(userId);

            session.insert(user);
            WeekLog week = insertLoggedWeek(session, userId, today);

            int firedRules = session.fireAllRules();
            Map<String, Object> result = baseResult("cep", firedRules, user, week.todayIntake, session);
            result.put("scenario", "Seven days of realistic user meal logging with skipped breakfasts, late meals, snacks, and daily aggregates.");
            result.put("loggedWeek", dailyLog(session));
            result.put("loggedMeals", mealLog(week.meals));
            result.put("patterns", patternNames(session));
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/week")
    public Map<String, Object> runWeeklyLoggingDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 20L;
            LocalDate today = LocalDate.now().minusDays(1);
            User user = weightLossUser(userId);

            session.insert(user);
            session.insert(new WeightMeasured(userId, 72.4, today.minusDays(6).atTime(7, 20)));
            session.insert(new WeightMeasured(userId, 72.1, today.atTime(7, 10)));
            session.insert(new NewActivityRoutine(userId, 1.55, today.minusDays(2).atTime(18, 30)));
            WeekLog week = insertLoggedWeek(session, userId, today);

            int firedRules = session.fireAllRules();
            QueryResults overeating = session.getQueryResults("whyOvereatingDetected", userId);
            QueryResults protein = session.getQueryResults("whyProteinCritical", userId);
            QueryResults plan = session.getQueryResults("whyPlanChangeNeeded", userId);

            Map<String, Object> result = baseResult("weekly-user-logging", firedRules, user, week.todayIntake, session);
            result.put("story", story(
                "Full week: skipped breakfast, evening compensation, low protein, and muscle-loss risk",
                "A 28-year-old woman with a weight-loss goal logs every meal for a week. She often skips breakfast, snacks repeatedly in the afternoon, eats several late meals, and usually misses her protein target.",
                "This demonstrates the PDF idea: the system is not a passive calorie notebook. Drools links meal timing, daily totals, weekly summaries, and weight/activity events into explainable warnings."
            ));
            result.put("loggedWeek", dailyLog(session));
            result.put("loggedMeals", mealLog(week.meals));
            result.put("patterns", patternNames(session));
            result.put("findings", findings(session));
            Map<String, Object> queries = new LinkedHashMap<>();
            queries.put("whyOvereatingDetected", overeating.size() > 0);
            queries.put("whyProteinCritical", protein.size() > 0);
            queries.put("whyPlanChangeNeeded", plan.size() > 0);
            result.put("backwardQueries", queries);
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/story/binge")
    public Map<String, Object> runBingeStoryDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 30L;
            LocalDate date = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();
            User user = weightLossUser(userId);
            DailyIntake intake = new DailyIntake(date, 0, 0, 0, 0, null, userId);
            List<Meal> meals = java.util.Arrays.asList(
                meal("Normalan dorucak - jaja i tost", MealCategory.BREAKFAST, 390, 24, now.minusHours(5), userId),
                meal("Rucak - piletina i krompir", MealCategory.LUNCH, 610, 38, now.minusHours(2), userId),
                meal("Prva grickalica posle stresa", MealCategory.SNACK, 260, 5, now.minusMinutes(70), userId),
                meal("Druga grickalica uz seriju", MealCategory.SNACK, 310, 4, now.minusMinutes(35), userId),
                meal("Treca grickalica bez plana", MealCategory.SNACK, 280, 3, now.minusMinutes(10), userId)
            );

            session.insert(user);
            session.insert(intake);
            meals.forEach(session::insert);

            int firedRules = session.fireAllRules();
            Map<String, Object> result = baseResult("story-binge-pattern", firedRules, user, intake, session);
            result.put("story", story(
                "Binge pattern after a stressful afternoon",
                "The user logs three snacks inside about 70 minutes after already logging breakfast and lunch. A calorie tracker would only sum this; the rule system notices the temporal cluster.",
                "Look at the timestamps. The system sees three eating events in a 90-minute window and inserts BingePattern, then returns advice to pause and stabilize the next meal."
            ));
            result.put("loggedMeals", mealLog(meals));
            result.put("patterns", patternNames(session));
            result.put("findings", findings(session));
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/story/skipped-breakfast-overeating")
    public Map<String, Object> runSkippedBreakfastOvereatingStory() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 31L;
            LocalDate today = LocalDate.now().minusDays(1);
            User user = weightLossUser(userId);
            DailyIntake intake = new DailyIntake(today, 0, 0, 0, 0, null, userId);
            List<Meal> meals = java.util.Arrays.asList(
                meal("Veliki rucak nakon preskocenog dorucka", MealCategory.LUNCH, 940, 28, today.atTime(14, 20), userId),
                meal("Slatka uzina", MealCategory.SNACK, 260, 4, today.atTime(17, 5), userId),
                meal("Kasna vecera - pasta", MealCategory.DINNER, 830, 19, today.atTime(22, 35), userId)
            );

            session.insert(user);
            session.insert(intake);
            for (int daysAgo = 3; daysAgo >= 1; daysAgo--) {
                session.insert(new MealSkipped(userId, MealCategory.BREAKFAST, today.minusDays(daysAgo)));
            }
            meals.forEach(session::insert);

            int firedRules = session.fireAllRules();
            QueryResults query = session.getQueryResults("whyOvereatingDetected", userId);
            Map<String, Object> result = baseResult("story-skipped-breakfast-overeating", firedRules, user, intake, session);
            result.put("story", story(
                "Metabolic domino: skipped breakfast leads to overeating",
                "For three days the user explicitly marks breakfast as skipped. On the next logged day, the first real meal is a large lunch above 40% of the daily target.",
                "The system inserts BreakfastSkippingPattern from the skipped meals, derives ENERGY_SHORTAGE, then connects that state with a high-calorie meal and concludes OVEREATING_DETECTED."
            ));
            result.put("skippedBreakfastDates", java.util.Arrays.asList(today.minusDays(3), today.minusDays(2), today.minusDays(1)));
            result.put("loggedMeals", mealLog(meals));
            result.put("patterns", patternNames(session));
            result.put("findings", findings(session));
            result.put("backwardQuery", queryResult("whyOvereatingDetected", query));
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/story/protein-critical")
    public Map<String, Object> runProteinCriticalStory() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 32L;
            LocalDate today = LocalDate.now().minusDays(1);
            User user = weightLossUser(userId);
            user.initializeTargets();

            session.insert(user);
            List<Meal> meals = new ArrayList<>();
            for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
                LocalDate date = today.minusDays(daysAgo);
                DailyIntake intake = new DailyIntake(date, 0, 0, 0, 0, null, userId);
                session.insert(intake);
                addMeal(session, meals, "Dorucak niskog proteina " + date, MealCategory.BREAKFAST, 330, 8, date.atTime(8, 30), userId);
                addMeal(session, meals, "Rucak bez dovoljno proteina " + date, MealCategory.LUNCH, 560, 18, date.atTime(13, 15), userId);
                if (daysAgo <= 4) {
                    addMeal(session, meals, "Uzina sa malo proteina " + date, MealCategory.SNACK, 230, 4, date.atTime(17, 10), userId);
                }
            }
            session.insert(new WeeklyPattern("protein-risk-week", 0, 0, 7840, 1120, WeightTrend.NEGATIVE, 35, userId));

            int firedRules = session.fireAllRules();
            QueryResults query = session.getQueryResults("whyProteinCritical", userId);
            Map<String, Object> result = baseResult("story-protein-critical", firedRules, user, null, session);
            result.put("story", story(
                "Protein critical risk during weight loss",
                "The user is trying to lose weight, but five or more days are far below 70% of her protein target and the weekly weight trend is negative.",
                "The system does not just say calories are low. It detects ConsistentProteinDeficit, combines it with negative weight trend, and flags muscle-loss/protein-critical risk."
            ));
            result.put("loggedWeek", dailyLog(session));
            result.put("loggedMeals", mealLog(meals));
            result.put("patterns", patternNames(session));
            result.put("findings", findings(session));
            result.put("backwardQuery", queryResult("whyProteinCritical", query));
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/story/metabolic-adaptation")
    public Map<String, Object> runMetabolicAdaptationStory() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 33L;
            LocalDate today = LocalDate.now().minusDays(1);
            User user = weightLossUser(userId);
            user.initializeTargets();
            WeeklyPattern weeklyPattern = new WeeklyPattern("stagnation-week", 0, 0, 4200, 600, WeightTrend.STABLE, 50, userId);
            weeklyPattern.setWeightVarianceKg(0.1);

            session.insert(user);
            session.insert(weeklyPattern);
            session.insert(new WeightMeasured(userId, 72.2, today.minusDays(13).atTime(7, 0)));
            session.insert(new WeightMeasured(userId, 72.1, today.minusDays(6).atTime(7, 0)));
            session.insert(new WeightMeasured(userId, 72.1, today.atTime(7, 0)));

            int firedRules = session.fireAllRules();
            QueryResults query = session.getQueryResults("whyPlanChangeNeeded", userId);
            Map<String, Object> result = baseResult("story-metabolic-adaptation", firedRules, user, null, session);
            result.put("story", story(
                "Stagnation despite extreme restriction",
                "For two weeks the weekly summary shows almost no weight variance while average calories are unrealistically low. This is the adaptation/stagnation case from the PDF.",
                "Drools detects WeightStagnationDetected, confirms the deficit context, then raises CALORIC_ADAPTATION_BLOCK and PLAN_CHANGE_NEEDED instead of telling the user to restrict even harder."
            ));
            result.put("weeklyPattern", weeklyPatternLog(weeklyPattern));
            result.put("findings", findings(session));
            result.put("backwardQuery", queryResult("whyPlanChangeNeeded", query));
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/templates")
    public Map<String, Object> runTemplateDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 3L;
            User user = weightLossUser(userId);
            user.initializeTargets();
            DailyIntake intake = new DailyIntake(LocalDate.now(), user.getTargetCalories() * 1.2, 40, 10, 3, null, userId);

            session.insert(new RuleTemplateParameter("Rule_Calorie_Surplus", ">", 1.10, "Any", "SURPLUS", "WARNING"));
            session.insert(new RuleTemplateParameter("Rule_Calorie_Deficit", "<", 0.70, "Any", "DEFICIT", "WARNING"));
            session.insert(user);
            session.insert(intake);

            int firedRules = session.fireAllRules();
            Map<String, Object> result = baseResult("rule-templates", firedRules, user, intake, session);
            result.put("templateFiles", list(
                "kjar/src/main/resources/templates/calorie-balance.drt",
                "kjar/src/main/resources/templates/calorie-balance.csv",
                "kjar/src/main/resources/templates/temporal-patterns.drt",
                "kjar/src/main/resources/templates/temporal-patterns.csv"
            ));
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/scenario/calorie-deficit")
    public Map<String, Object> runCalorieDeficitDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 7L;
            User user = weightLossUser(userId);
            user.initializeTargets();
            DailyIntake intake = new DailyIntake(LocalDate.now(), user.getTargetCalories() * 0.6, 35, 8, 2, null, userId);
            session.insert(user);
            session.insert(intake);
            int firedRules = session.fireAllRules();
            return baseResult("calorie-deficit", firedRules, user, intake, session);
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/scenario/maintenance")
    public Map<String, Object> runMaintenanceDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 8L;
            User user = weightLossUser(userId);
            user.setGoal(GoalType.MAINTENANCE);
            user.initializeTargets();
            DailyIntake intake = new DailyIntake(LocalDate.now(), user.getTargetCalories(), user.getTargetProtein(), 25, 3, null, userId);
            session.insert(user);
            session.insert(intake);
            int firedRules = session.fireAllRules();
            return baseResult("maintenance-stability", firedRules, user, intake, session);
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/scenario/mass-gain-conflict")
    public Map<String, Object> runMassGainConflictDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 9L;
            User user = weightLossUser(userId);
            user.setGoal(GoalType.MUSCLE_GAIN);
            user.initializeTargets();
            DailyIntake intake = new DailyIntake(LocalDate.now(), user.getTargetCalories() * 0.55, 70, 12, 2, null, userId);
            session.insert(user);
            session.insert(intake);
            int firedRules = session.fireAllRules();
            return baseResult("mass-gain-conflict", firedRules, user, intake, session);
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/scenario/protein-distribution")
    public Map<String, Object> runProteinDistributionDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 10L;
            LocalDateTime now = LocalDateTime.now();
            User user = weightLossUser(userId);
            DailyIntake intake = new DailyIntake(LocalDate.now(), 0, 0, 0, 0, null, userId);
            session.insert(user);
            session.insert(intake);
            for (int i = 0; i < 4; i++) {
                session.insert(meal("Low protein meal " + i, MealCategory.SNACK, 250, 8, now.minusHours(i), userId));
            }
            int firedRules = session.fireAllRules();
            return baseResult("protein-distribution-conflict", firedRules, user, intake, session);
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/scenario/high-protein-hydration")
    public Map<String, Object> runHighProteinHydrationDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 11L;
            User user = weightLossUser(userId);
            user.initializeTargets();
            DailyIntake intake = new DailyIntake(LocalDate.now(), 1900, user.getWeight() * 2.4, 20, 4, null, userId);
            session.insert(user);
            session.insert(intake);
            int firedRules = session.fireAllRules();
            return baseResult("high-protein-hydration", firedRules, user, intake, session);
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/scenario/long-fast")
    public Map<String, Object> runLongFastDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 12L;
            LocalDateTime today = LocalDate.now().atTime(8, 0);
            User user = weightLossUser(userId);
            DailyIntake intake = new DailyIntake(LocalDate.now(), 0, 0, 0, 0, null, userId);
            session.insert(user);
            session.insert(intake);
            session.insert(meal("Breakfast", MealCategory.BREAKFAST, 350, 22, today, userId));
            session.insert(meal("Late lunch", MealCategory.LUNCH, 600, 30, today.plusHours(7), userId));
            int firedRules = session.fireAllRules();
            Map<String, Object> result = baseResult("long-fast", firedRules, user, intake, session);
            result.put("patterns", patternNames(session));
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/scenario/activity-weight")
    public Map<String, Object> runActivityAndWeightDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 13L;
            LocalDateTime now = LocalDateTime.now();
            User user = weightLossUser(userId);
            user.initializeTargets();
            session.insert(user);
            session.insert(new NewActivityRoutine(userId, 1.725, now));
            session.insert(new WeightMeasured(userId, 72, now.minusDays(6)));
            session.insert(new WeightMeasured(userId, 74.4, now));
            int firedRules = session.fireAllRules();
            Map<String, Object> result = baseResult("activity-change-and-weight-oscillation", firedRules, user, null, session);
            result.put("newActivityFactor", user.getActivityFactor());
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/backward/overeating")
    public Map<String, Object> runBackwardOvereatingDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 4L;
            insertOvereatingChain(session, userId);
            int firedRules = session.fireAllRules();
            QueryResults queryResults = session.getQueryResults("whyOvereatingDetected", userId);
            Map<String, Object> result = baseResult("backward-chaining-overeating", firedRules, null, null, session);
            result.put("query", "whyOvereatingDetected(" + userId + ")");
            result.put("queryMatched", queryResults.size() > 0);
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/backward/protein")
    public Map<String, Object> runBackwardProteinDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 5L;
            insertProteinCriticalChain(session, userId);
            int firedRules = session.fireAllRules();
            QueryResults queryResults = session.getQueryResults("whyProteinCritical", userId);
            Map<String, Object> result = baseResult("backward-chaining-protein", firedRules, null, null, session);
            result.put("query", "whyProteinCritical(" + userId + ")");
            result.put("queryMatched", queryResults.size() > 0);
            return result;
        } finally {
            session.dispose();
        }
    }

    @GetMapping("/backward/plan")
    public Map<String, Object> runBackwardPlanDemo() {
        KieSession session = kieContainer.newKieSession("nutritionKSession");
        try {
            Long userId = 6L;
            insertPlanChangeChain(session, userId);
            int firedRules = session.fireAllRules();
            QueryResults queryResults = session.getQueryResults("whyPlanChangeNeeded", userId);
            Map<String, Object> result = baseResult("backward-chaining-plan", firedRules, null, null, session);
            result.put("query", "whyPlanChangeNeeded(" + userId + ")");
            result.put("queryMatched", queryResults.size() > 0);
            return result;
        } finally {
            session.dispose();
        }
    }

    private void insertOvereatingChain(KieSession session, Long userId) {
        LocalDate today = LocalDate.now();
        User user = weightLossUser(userId);
        session.insert(user);
        session.insert(new DailyIntake(today, 0, 0, 0, 0, null, userId));
        for (int i = 1; i <= 3; i++) {
            session.insert(new MealSkipped(userId, MealCategory.BREAKFAST, today.minusDays(i)));
        }
        session.insert(meal("Kompenzacioni obrok", MealCategory.DINNER, 900, 15, LocalDateTime.now(), userId));
    }

    private void insertProteinCriticalChain(KieSession session, Long userId) {
        LocalDate today = LocalDate.now();
        User user = weightLossUser(userId);
        user.initializeTargets();
        session.insert(user);
        session.insert(new DailyIntake(today, 0, 0, 0, 0, null, userId));
        for (int i = 1; i <= 5; i++) {
            session.insert(new DailyIntake(today.minusDays(i), 1200, 45, 10, 3, DailyIntakeStatus.DEFICIT, userId));
        }
        session.insert(new WeeklyPattern("protein-demo", 0, 0, 8400, 1200, WeightTrend.NEGATIVE, 45, userId));
        session.insert(meal("Slab proteinski obrok", MealCategory.LUNCH, 500, 10, LocalDateTime.now(), userId));
    }

    private void insertPlanChangeChain(KieSession session, Long userId) {
        User user = weightLossUser(userId);
        user.initializeTargets();
        WeeklyPattern weeklyPattern = new WeeklyPattern("plan-demo", 0, 0, 4200, 600, WeightTrend.STABLE, 65, userId);
        weeklyPattern.setWeightVarianceKg(0.1);
        session.insert(user);
        session.insert(weeklyPattern);
    }

    private WeekLog insertLoggedWeek(KieSession session, Long userId, LocalDate today) {
        WeekLog week = new WeekLog();

        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            LocalDate date = today.minusDays(daysAgo);
            DailyIntake intake = new DailyIntake(date, 0, 0, 0, 0, null, userId);
            session.insert(intake);
            if (date.equals(today)) {
                week.todayIntake = intake;
            }
        }

        addMeal(session, week, "Ponedeljak dorucak - tost i jaja", MealCategory.BREAKFAST, 360, 22, today.minusDays(6).atTime(8, 5), userId);
        addMeal(session, week, "Ponedeljak rucak - piletina salata", MealCategory.LUNCH, 610, 38, today.minusDays(6).atTime(13, 10), userId);
        addMeal(session, week, "Ponedeljak uzina - proteinski jogurt", MealCategory.SNACK, 180, 18, today.minusDays(6).atTime(16, 40), userId);
        addMeal(session, week, "Ponedeljak vecera - losos i krompir", MealCategory.DINNER, 690, 42, today.minusDays(6).atTime(20, 15), userId);

        session.insert(new MealSkipped(userId, MealCategory.BREAKFAST, today.minusDays(5)));
        addMeal(session, week, "Utorak kafa umesto dorucka", MealCategory.SNACK, 90, 2, today.minusDays(5).atTime(9, 15), userId);
        addMeal(session, week, "Utorak sendvic iz pekare", MealCategory.LUNCH, 760, 24, today.minusDays(5).atTime(13, 35), userId);
        addMeal(session, week, "Utorak slatka uzina", MealCategory.SNACK, 280, 4, today.minusDays(5).atTime(17, 20), userId);
        addMeal(session, week, "Utorak kasna pasta", MealCategory.DINNER, 840, 19, today.minusDays(5).atTime(22, 35), userId);

        addMeal(session, week, "Sreda ovsena kasa", MealCategory.BREAKFAST, 430, 21, today.minusDays(4).atTime(8, 20), userId);
        addMeal(session, week, "Sreda rucak - riza i povrce", MealCategory.LUNCH, 590, 17, today.minusDays(4).atTime(14, 0), userId);
        addMeal(session, week, "Sreda energetska plocica", MealCategory.SNACK, 260, 6, today.minusDays(4).atTime(18, 10), userId);
        addMeal(session, week, "Sreda kasni tost", MealCategory.SNACK, 310, 8, today.minusDays(4).atTime(23, 15), userId);

        session.insert(new MealSkipped(userId, MealCategory.BREAKFAST, today.minusDays(3)));
        addMeal(session, week, "Cetvrtak veliki rucak posle preskakanja", MealCategory.LUNCH, 940, 25, today.minusDays(3).atTime(14, 30), userId);
        addMeal(session, week, "Cetvrtak keksi", MealCategory.SNACK, 220, 3, today.minusDays(3).atTime(16, 5), userId);
        addMeal(session, week, "Cetvrtak vecera - tortilja", MealCategory.DINNER, 680, 24, today.minusDays(3).atTime(20, 45), userId);

        addMeal(session, week, "Petak dorucak - smoothie", MealCategory.BREAKFAST, 330, 11, today.minusDays(2).atTime(8, 50), userId);
        addMeal(session, week, "Petak rucak - testenina", MealCategory.LUNCH, 720, 18, today.minusDays(2).atTime(13, 45), userId);
        addMeal(session, week, "Petak kokice", MealCategory.SNACK, 190, 4, today.minusDays(2).atTime(21, 50), userId);
        addMeal(session, week, "Petak nocna uzina", MealCategory.SNACK, 260, 7, today.minusDays(2).atTime(23, 40), userId);

        session.insert(new MealSkipped(userId, MealCategory.BREAKFAST, today.minusDays(1)));
        addMeal(session, week, "Subota brunch - burger", MealCategory.LUNCH, 980, 31, today.minusDays(1).atTime(12, 40), userId);
        addMeal(session, week, "Subota cips", MealCategory.SNACK, 260, 4, today.minusDays(1).atTime(18, 15), userId);
        addMeal(session, week, "Subota pica", MealCategory.DINNER, 890, 28, today.minusDays(1).atTime(21, 30), userId);

        addMeal(session, week, "Nedelja dorucak - pecivo", MealCategory.BREAKFAST, 430, 10, today.atTime(8, 35), userId);
        addMeal(session, week, "Nedelja rucak - pileca supa", MealCategory.LUNCH, 520, 27, today.atTime(13, 5), userId);
        addMeal(session, week, "Nedelja brza uzina 1", MealCategory.SNACK, 180, 4, today.atTime(16, 40), userId);
        addMeal(session, week, "Nedelja brza uzina 2", MealCategory.SNACK, 210, 5, today.atTime(17, 15), userId);
        addMeal(session, week, "Nedelja brza uzina 3", MealCategory.SNACK, 240, 5, today.atTime(17, 40), userId);
        addMeal(session, week, "Nedelja kasna vecera", MealCategory.DINNER, 780, 18, today.atTime(22, 25), userId);

        WeeklyPattern weeklyPattern = new WeeklyPattern(
            "week-" + today,
            3,
            4,
            13270,
            1895,
            WeightTrend.NEGATIVE,
            57,
            userId
        );
        weeklyPattern.setWeightVarianceKg(0.3);
        session.insert(weeklyPattern);

        return week;
    }

    private void addMeal(KieSession session, WeekLog week, String name, MealCategory category, double calories, double protein, LocalDateTime timestamp, Long userId) {
        Meal loggedMeal = meal(name, category, calories, protein, timestamp, userId);
        week.meals.add(loggedMeal);
        session.insert(loggedMeal);
    }

    private void addMeal(KieSession session, List<Meal> meals, String name, MealCategory category, double calories, double protein, LocalDateTime timestamp, Long userId) {
        Meal loggedMeal = meal(name, category, calories, protein, timestamp, userId);
        meals.add(loggedMeal);
        session.insert(loggedMeal);
    }

    private User weightLossUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setGender(Gender.FEMALE);
        user.setAge(28);
        user.setWeight(72);
        user.setHeight(168);
        user.setGoal(GoalType.WEIGHT_LOSS);
        user.setActivityFactor(1.375);
        return user;
    }

    private Meal meal(String name, MealCategory category, double calories, double protein, LocalDateTime timestamp, Long userId) {
        return new MealLogged(name, category, calories, protein, calories * 0.12, calories * 0.03, 5, 8, timestamp, userId);
    }

    private Map<String, Object> baseResult(String demo, int firedRules, User user, DailyIntake intake, KieSession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("demo", demo);
        result.put("firedRules", firedRules);
        if (user != null) {
            result.put("targetCalories", Math.round(user.getTargetCalories()));
            result.put("targetProtein", Math.round(user.getTargetProtein()));
        }
        if (intake != null) {
            result.put("todayCalories", intake.getTotalCalories());
            result.put("todayProtein", intake.getTotalProtein());
            result.put("dailyStatus", intake.getStatus());
        }
        result.put("analyticStatuses", statuses(session).stream().map(AnalyticStatus::getType).map(AnalyticStatusType::name).collect(Collectors.toList()));
        result.put("recommendations", recommendations(session).stream().map(Recommendation::getMessage).collect(Collectors.toList()));
        return result;
    }

    private List<Map<String, Object>> dailyLog(KieSession session) {
        return session.getObjects(object -> object instanceof DailyIntake)
            .stream()
            .map(DailyIntake.class::cast)
            .sorted(Comparator.comparing(DailyIntake::getDate))
            .map(intake -> {
                Map<String, Object> day = new LinkedHashMap<>();
                day.put("date", intake.getDate());
                day.put("calories", Math.round(intake.getTotalCalories()));
                day.put("protein", Math.round(intake.getTotalProtein()));
                day.put("fiber", Math.round(intake.getTotalFiber()));
                day.put("mealCount", intake.getMealCount());
                day.put("status", intake.getStatus());
                return day;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> mealLog(List<Meal> meals) {
        return meals.stream()
            .sorted(Comparator.comparing(Meal::getTimestamp))
            .map(meal -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("timestamp", meal.getTimestamp());
                entry.put("category", meal.getCategory());
                entry.put("name", meal.getName());
                entry.put("calories", meal.getCalories());
                entry.put("protein", meal.getProtein());
                return entry;
            })
            .collect(Collectors.toList());
    }

    private Map<String, Object> story(String title, String userExperience, String reasoning) {
        Map<String, Object> story = new LinkedHashMap<>();
        story.put("title", title);
        story.put("userExperience", userExperience);
        story.put("reasoning", reasoning);
        return story;
    }

    private Map<String, Object> findings(KieSession session) {
        Map<String, Object> findings = new LinkedHashMap<>();
        findings.put("analyticStatusesFromDrools", statuses(session).stream()
            .map(status -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("type", status.getType().name());
                entry.put("severity", status.getSeverity());
                entry.put("expiresAt", status.getExpiresAt());
                return entry;
            })
            .collect(Collectors.toList()));
        findings.put("patternsFromDrools", session.getObjects(this::isPattern)
            .stream()
            .map(pattern -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("type", pattern.getClass().getSimpleName());
                NutritionPattern nutritionPattern = (NutritionPattern) pattern;
                entry.put("detectedAt", nutritionPattern.getDetectedAt());
                entry.put("description", nutritionPattern.getDescription());
                return entry;
            })
            .collect(Collectors.toList()));
        findings.put("recommendationsFromDrools", recommendations(session).stream()
            .map(recommendation -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("type", recommendation.getType());
                entry.put("message", recommendation.getMessage());
                return entry;
            })
            .collect(Collectors.toList()));
        return findings;
    }

    private Map<String, Object> queryResult(String queryName, QueryResults queryResults) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", queryName);
        result.put("matched", queryResults.size() > 0);
        result.put("matchedRows", queryResults.size());
        return result;
    }

    private Map<String, Object> weeklyPatternLog(WeeklyPattern weeklyPattern) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("weekId", weeklyPattern.getWeekId());
        entry.put("skippedMealCount", weeklyPattern.getSkippedMealCount());
        entry.put("lateMealCount", weeklyPattern.getLateMealCount());
        entry.put("totalCalories", weeklyPattern.getTotalCalories());
        entry.put("averageCalories", weeklyPattern.getAverageCalories());
        entry.put("weightTrend", weeklyPattern.getWeightTrend());
        entry.put("proteinTrend", weeklyPattern.getProteinTrend());
        entry.put("weightVarianceKg", weeklyPattern.getWeightVarianceKg());
        return entry;
    }

    private List<AnalyticStatus> statuses(KieSession session) {
        return session.getObjects(object -> object instanceof AnalyticStatus)
            .stream()
            .map(AnalyticStatus.class::cast)
            .sorted(Comparator.comparing(status -> status.getType().name()))
            .collect(Collectors.toList());
    }

    private List<Recommendation> recommendations(KieSession session) {
        return session.getObjects(object -> object instanceof Recommendation)
            .stream()
            .map(Recommendation.class::cast)
            .sorted(Comparator.comparing(Recommendation::getType))
            .collect(Collectors.toList());
    }

    private List<String> patternNames(KieSession session) {
        return session.getObjects(this::isPattern)
            .stream()
            .map(object -> object.getClass().getSimpleName())
            .sorted()
            .collect(Collectors.toList());
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

    private List<String> list(String... values) {
        return java.util.Arrays.asList(values);
    }

    private static class WeekLog {
        private final List<Meal> meals = new ArrayList<>();
        private DailyIntake todayIntake;
    }
}
