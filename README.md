# Adaptive Nutrition Expert System

Knowledge-based system for adaptive personalized nutrition. The project is split into three Maven modules:

- `model` - Java facts, primitive events, enums, CEP pattern classes, and helper model objects.
- `kjar` - Drools knowledge base, rule templates, generated template rules, CEP rules, forward rules, and backward-chaining queries.
- `service` - Spring Boot application that exposes runnable demos.

Java and Drools versions were not changed. The project still uses Java 11 and Drools `7.49.0.Final` in the Drools modules.

## Implemented Model

The model contains the main facts from the specification:

- `User` with gender, age, weight, height, goal, PAL/activity factor, allergies, BMR, target calories, target protein, and target fiber.
- `Meal` as a Drools event with category, calories, protein, carbohydrates, fat, fiber, sugars, timestamp, and user id.
- `DailyIntake` with daily totals, average calories, protein, fiber, meal count, user id, and status.
- `WeeklyPattern` with skipped meal count, late meal count, total/average calories, protein trend, weight trend, weight variance, and user id.
- `AnalyticStatus` and `Recommendation` for inferred states and user-facing output.
- Primitive events: `MealSkipped`, `WeightMeasurement`, `ActivityChange`.
- Primitive events package: `model.events` contains `MealLogged`, `MealSkipped`, `WeightMeasured`, and `NewActivityRoutine`, matching the PDF specification.
- CEP pattern facts: `BreakfastSkippingPattern`, `LateNightEatingPattern`, `ConsistentProteinDeficit`, `WeightStagnationDetected`, `LongFastDetected`, `UnplannedSnackingPattern`, `BingePattern`.
- Template support fact: `RuleTemplateParameter`.

## Implemented Rules

Rules are in:

- `kjar/src/main/resources/rules/nutrition-rules.drl`
- `kjar/src/main/resources/rules/generated-template-rules.drl`

Implemented forward-chaining rules include:

- Initialize user target values from BMR/TDEE.
- Update daily intake from meal events.
- Calorie surplus.
- Calorie deficit.
- Maintenance stability.
- Weight-loss conflict.
- Mass-gain conflict.
- Low-protein meal.
- Protein warning.
- Energy-shortage link.
- Overtaking/overeating linkage.
- Muscle-loss risk.
- Protein critical.
- Weight-stagnation confirmation.
- Extreme caloric restriction.
- Caloric adaptation block.
- Plan change needed.
- Protein floor.
- Protein distribution conflict.
- High-protein hydration.
- Calorie spike.
- Long fast.
- Activity increase recommendation.
- Weight oscillation recommendation.

Implemented CEP rules include:

- Breakfast skipping from repeated skipped breakfast events.
- Late-night eating spike.
- Late-night eating pattern.
- Binge pattern: 3 meal events in a 90-minute event window.
- Unplanned snacking.
- Consistent protein deficit over repeated daily intake facts.
- Weight stagnation from weekly weight variance.

Implemented backward-chaining queries:

- `whyOvereatingDetected(Long userId)`
- `whyProteinCritical(Long userId)`
- `whyPlanChangeNeeded(Long userId)`

## Implemented Rule Templates

Template files are in:

- `kjar/src/main/resources/templates/calorie-balance.drt`
- `kjar/src/main/resources/templates/calorie-balance.csv`
- `kjar/src/main/resources/templates/temporal-patterns.drt`
- `kjar/src/main/resources/templates/temporal-patterns.csv`

Runtime-generated equivalents are included in:

- `kjar/src/main/resources/rules/generated-template-rules.drl`

The service also demonstrates runtime threshold facts through `RuleTemplateParameter` in `/api/demo/templates`.

## Build Instructions

Run these from PowerShell. Build order matters because `kjar` depends on `model`, and `service` depends on both.

```powershell
& "D:\desktop\faks\vjezbe sbnz\Adaptive-Nutrition-Expert-System\model\mvnw.cmd" install -f "D:\desktop\faks\vjezbe sbnz\Adaptive-Nutrition-Expert-System\model\pom.xml"

& "D:\desktop\faks\vjezbe sbnz\Adaptive-Nutrition-Expert-System\kjar\mvnw.cmd" install -f "D:\desktop\faks\vjezbe sbnz\Adaptive-Nutrition-Expert-System\kjar\pom.xml"

& "D:\desktop\faks\vjezbe sbnz\Adaptive-Nutrition-Expert-System\service\mvnw.cmd" install -f "D:\desktop\faks\vjezbe sbnz\Adaptive-Nutrition-Expert-System\service\pom.xml"
```

Run the service:

```powershell
cd "D:\desktop\faks\vjezbe sbnz\Adaptive-Nutrition-Expert-System\service"
java -jar target\service-0.0.1-SNAPSHOT.jar
```

## Demo Endpoints

Call endpoints from a second PowerShell window while the service is running.

The demos exist because this is a knowledge-based system, not a normal CRUD application. In a normal application you call one method and get one calculated answer. In Drools, you insert facts into working memory, run `fireAllRules()`, and the rule engine decides which rules match. The demos create controlled fact sets to prove that forward chaining, CEP, templates, and backward-style explanation queries work.

This command:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/forward | ConvertTo-Json -Depth 8
```

does three things:

1. Sends an HTTP GET request to the running Spring Boot app at `/api/demo/forward`.
2. The controller creates a sample user, a daily intake object, and one meal, then inserts them into `nutritionKSession`.
3. Drools fires all matching rules and the controller converts the resulting facts into JSON.


The fields in the output mean:

- `demo` - which scenario was executed.
- `firedRules` - how many Drools rules activated and ran.
- `targetCalories` and `targetProtein` - personalized targets inferred from the user's BMR/TDEE and goal.
- `todayCalories` and `todayProtein` - the current daily aggregate after meal facts were processed.
- `dailyStatus` - the inferred daily energy status, for example `SURPLUS`.
- `analyticStatuses` - internal facts inserted by rules. These are the machine-readable conclusions.
- `recommendations` - user-facing messages derived from those statuses.
- `triggeredBy` - short description of the demo input.
- `expectedHighlights` - what the demo is designed to prove.

Forward chaining demo:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/forward | ConvertTo-Json -Depth 8
```

Expected highlights:

- `firedRules`: `9`
- `dailyStatus`: `SURPLUS`
- `analyticStatuses`: `CALORIC_SURPLUS_ACCUMULATION`, `CALORIE_SPIKE_DETECTED`, `CIRCADIAN_RHYTHM_DISRUPTION`, `LOW_PROTEIN_MEAL`, `PROTEIN_INSUFFICIENT`, `PROTEIN_WARNING`

CEP demo:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/cep | ConvertTo-Json -Depth 8
```

Expected highlights:

- `firedRules`: `15`
- `patterns`: `BingePattern`, `BreakfastSkippingPattern`, `LateNightEatingPattern`, `UnplannedSnackingPattern`
- `analyticStatuses`: `ENERGY_SHORTAGE`, `LOW_PROTEIN_MEAL`, `PROTEIN_WARNING`

Template demo:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/templates | ConvertTo-Json -Depth 8
```

Expected highlights:

- `firedRules`: `2`
- `dailyStatus`: `SURPLUS`
- `templateFiles` lists the `.drt` and `.csv` files.

Backward chaining - overeating:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/backward/overeating | ConvertTo-Json -Depth 8
```

Expected highlights:

- `query`: `whyOvereatingDetected(4)`
- `queryMatched`: `true`
- Explanation chain: skipped breakfasts -> `BreakfastSkippingPattern` -> `ENERGY_SHORTAGE` -> high-calorie later meal -> `OVEREATING_DETECTED`.

Backward chaining - protein critical:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/backward/protein | ConvertTo-Json -Depth 8
```

Expected highlights:

- `query`: `whyProteinCritical(5)`
- `queryMatched`: `true`
- Explanation chain: repeated protein deficit -> `ConsistentProteinDeficit` -> negative weight trend -> `MUSCLE_LOSS_RISK` -> low-protein meal -> `PROTEIN_CRITICAL`.

Backward chaining - plan change:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/backward/plan | ConvertTo-Json -Depth 8
```

Expected highlights:

- `query`: `whyPlanChangeNeeded(6)`
- `queryMatched`: `true`
- Explanation chain: low weight variance -> `WeightStagnationDetected` -> confirmed stagnation -> caloric adaptation block -> `PLAN_CHANGE_NEEDED`.

Run all demos:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/rules | ConvertTo-Json -Depth 10
```

## Additional Test Scenarios

These scenarios are small, focused tests. Each one creates a different set of facts and shows which rules fire.

### Calorie Deficit

Command:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/scenario/calorie-deficit | ConvertTo-Json -Depth 8
```

What happens:

- The demo creates a weight-loss user.
- The daily intake is set to 60% of the calculated calorie target.
- Protein is also below the minimum floor.

Expected output:

- `dailyStatus`: `DEFICIT`
- `analyticStatuses`: includes `PROTEIN_INSUFFICIENT`
- Verified `firedRules`: `2`

### Maintenance Stability

Command:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/scenario/maintenance | ConvertTo-Json -Depth 8
```

What happens:

- The user goal is changed to `MAINTENANCE`.
- Daily calories are set exactly around the calculated target.

Expected output:

- `dailyStatus`: `MAINTENANCE_STABLE`
- Verified `firedRules`: `1`

### Mass-Gain Conflict

Command:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/scenario/mass-gain-conflict | ConvertTo-Json -Depth 8
```

What happens:

- The user goal is `MUSCLE_GAIN`.
- Daily intake is far below the target.
- Drools detects that this behavior conflicts with gaining muscle mass.

Expected output:

- `dailyStatus`: `DEFICIT`
- `analyticStatuses`: includes `EXTREME_CALORIC_RESTRICTION`, `PLAN_CHANGE_NEEDED`, `PROTEIN_INSUFFICIENT`
- Recommendations include a mass-gain conflict message.
- Verified `firedRules`: `4`

### Protein Distribution Conflict

Command:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/scenario/protein-distribution | ConvertTo-Json -Depth 8
```

What happens:

- The demo inserts four meal events with less than 15g protein.
- Drools detects low-protein meals and then the higher-level distribution conflict.

Expected output:

- `analyticStatuses`: includes `LOW_PROTEIN_MEAL`, `PROTEIN_DISTRIBUTION_CONFLICT`, `PROTEIN_INSUFFICIENT`, `PROTEIN_WARNING`
- Verified `firedRules`: `11`

### High-Protein Hydration

Command:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/scenario/high-protein-hydration | ConvertTo-Json -Depth 8
```

What happens:

- The daily protein intake is set above `2.2g/kg`.
- Drools recommends increasing hydration.

Expected output:

- `analyticStatuses`: includes `HIGH_PROTEIN_HYDRATION_NEEDED`
- Recommendations include a hydration message.
- Verified `firedRules`: `3`

### Long Fast

Command:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/scenario/long-fast | ConvertTo-Json -Depth 8
```

What happens:

- The demo inserts a breakfast at 08:00 and lunch seven hours later.
- Drools detects a metabolic gap during the active part of the day.

Expected output:

- `analyticStatuses`: includes `LONG_FAST_DETECTED`
- `patterns`: includes `LongFastDetected`
- Verified `firedRules`: `6`

### Activity Change And Weight Oscillation

Command:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/demo/scenario/activity-weight | ConvertTo-Json -Depth 8
```

What happens:

- The demo inserts a `NewActivityRoutine` event with PAL `1.725`.
- It also inserts two `WeightMeasured` events with more than 2kg change inside one week.
- Drools recalculates the user target and recommends checking weight-measurement consistency.

Expected output:

- `newActivityFactor`: `1.725`
- Recommendations include `WEIGHT_OSCILLATION`
- Verified `firedRules`: `2`

## Frontend-Compatible Endpoint

The demo endpoints use hardcoded sample data. A frontend should use:

```http
POST /api/nutrition/evaluate
```

Example request:

```json
{
  "user": {
    "id": 10,
    "gender": "FEMALE",
    "age": 28,
    "weight": 72,
    "height": 168,
    "goal": "WEIGHT_LOSS",
    "activityFactor": 1.375
  },
  "dailyIntake": {
    "date": "2026-05-24",
    "totalCalories": 0,
    "totalProtein": 0,
    "totalFiber": 0,
    "mealCount": 0,
    "userId": 10
  },
  "meals": [
    {
      "name": "Kasna vecera",
      "category": "DINNER",
      "calories": 2100,
      "protein": 12,
      "carbohydrates": 230,
      "fat": 75,
      "fiber": 6,
      "sugars": 48,
      "timestamp": "2026-05-24T22:30:00",
      "userId": 10
    }
  ],
  "previousDailyIntakes": [],
  "skippedMeals": []
}
```

Example call:

```powershell
$body = Get-Content .\request.json -Raw
Invoke-RestMethod -Uri http://localhost:8080/api/nutrition/evaluate -Method Post -Body $body -ContentType "application/json" | ConvertTo-Json -Depth 8
```

The response has a stable shape for frontend use:

```json
{
  "firedRules": 9,
  "targetCalories": 1520.0,
  "targetProtein": 100.8,
  "dailyStatus": "SURPLUS",
  "analyticStatuses": ["CALORIC_SURPLUS_ACCUMULATION", "CALORIE_SPIKE_DETECTED", "LOW_PROTEIN_MEAL"],
  "recommendations": ["..."],
  "detectedPatterns": []
}
```

## How This Works in Classic Drools Terms

The project follows the standard Drools split:

- Facts are plain Java classes in `model.models`.
- Primitive events are plain Java classes in `model.events`.
- Rules are `.drl` files in `kjar`.
- The KIE module is configured by `kmodule.xml`.
- Spring creates a `KieContainer`.
- Each request creates a `KieSession`.
- Java objects are inserted into working memory using `session.insert(...)`.
- Drools evaluates the left-hand side of each rule, runs matching right-hand sides, and inserts/modifies inferred facts.
- The service reads inferred facts from working memory and returns them as JSON.

Forward chaining means: start from known facts and infer new facts. Example: `Meal + DailyIntake + User -> SURPLUS -> CALORIC_SURPLUS_ACCUMULATION -> Recommendation`.

CEP means: treat some facts as events and reason over time/count patterns. Example: `3 MealSkipped(BREAKFAST)` facts create `BreakfastSkippingPattern`.

Backward chaining/explanation here is implemented with Drools queries. A query checks whether the facts needed to explain a conclusion exist in working memory. Example: `whyProteinCritical(userId)` verifies the chain `ConsistentProteinDeficit + negative weight trend + low protein meal -> PROTEIN_CRITICAL`.

Rule templates are represented as `.drt` plus `.csv` resources. They document the parameterized rule source, while `generated-template-rules.drl` contains the generated equivalent rules that are packaged into the KJAR.

## File Guide

- `model/src/main/java/com/ftn/sbnz/model/models/User.java` - user profile and BMR/TDEE target calculation.
- `model/src/main/java/com/ftn/sbnz/model/models/Meal.java` - base meal fact with nutrition payload and timestamp.
- `model/src/main/java/com/ftn/sbnz/model/events/MealLogged.java` - primitive meal event used by Drools CEP and forward chaining.
- `model/src/main/java/com/ftn/sbnz/model/events/MealSkipped.java` - primitive event for skipped meals.
- `model/src/main/java/com/ftn/sbnz/model/events/WeightMeasured.java` - primitive event for weight logs.
- `model/src/main/java/com/ftn/sbnz/model/events/NewActivityRoutine.java` - primitive event for PAL/activity changes.
- `model/src/main/java/com/ftn/sbnz/model/models/DailyIntake.java` - daily aggregate that rules modify.
- `model/src/main/java/com/ftn/sbnz/model/models/WeeklyPattern.java` - weekly aggregate/trend fact for long-term reasoning.
- `model/src/main/java/com/ftn/sbnz/model/models/AnalyticStatus.java` - inferred internal status fact.
- `model/src/main/java/com/ftn/sbnz/model/models/Recommendation.java` - final user-facing advice fact.
- `model/src/main/java/com/ftn/sbnz/model/models/MealSkipped.java`, `WeightMeasurement.java`, `ActivityChange.java` - legacy compatibility event models kept in place; the current Drools rules use `model.events`.
- `model/src/main/java/com/ftn/sbnz/model/models/*Pattern.java` - CEP pattern facts created by rules.
- `model/src/main/java/com/ftn/sbnz/model/models/RuleTemplateParameter.java` - runtime threshold fact used by template-style rules.
- `model/src/main/java/com/ftn/sbnz/model/enums/*` - controlled values for goals, categories, statuses, severity, gender, and trends.
- `kjar/src/main/resources/META-INF/kmodule.xml` - defines `nutritionKBase` and `nutritionKSession`.
- `kjar/src/main/resources/rules/nutrition-rules.drl` - main rules, CEP rules, and backward queries.
- `kjar/src/main/resources/rules/generated-template-rules.drl` - generated/runtime equivalent of the template rules.
- `kjar/src/main/resources/templates/*.drt` - Drools rule templates.
- `kjar/src/main/resources/templates/*.csv` - template parameter tables.
- `service/src/main/java/com/ftn/sbnz/service/ServiceApplication.java` - Spring Boot entry point and `KieContainer` bean.
- `service/src/main/java/com/ftn/sbnz/service/controller/NutritionDemoController.java` - hardcoded learning/demo scenarios.
- `service/src/main/java/com/ftn/sbnz/service/controller/NutritionEvaluationController.java` - frontend-ready evaluation API.
- `service/src/main/java/com/ftn/sbnz/service/dto/NutritionEvaluationRequest.java` - request body for frontend integration.
- `service/src/main/java/com/ftn/sbnz/service/dto/NutritionEvaluationResponse.java` - stable response body for frontend integration.
- `diagram.puml` - PlantUML UML class diagram.
- `diagram.png` - rendered diagram image.

## Verified Output Snapshot

The current implementation was verified with these demo results:

- `/api/demo/forward`: fired `9` rules and produced `SURPLUS`.
- `/api/demo/cep`: fired `15` rules and produced all four listed CEP pattern facts.
- `/api/demo/templates`: fired `2` rules and produced `SURPLUS`.
- `/api/demo/backward/overeating`: query matched `true`.
- `/api/demo/backward/protein`: query matched `true`.
- `/api/demo/backward/plan`: query matched `true`.
- `/api/demo/scenario/calorie-deficit`: produced `DEFICIT` and `PROTEIN_INSUFFICIENT`.
- `/api/demo/scenario/maintenance`: produced `MAINTENANCE_STABLE`.
- `/api/demo/scenario/mass-gain-conflict`: produced `DEFICIT`, `EXTREME_CALORIC_RESTRICTION`, and `PLAN_CHANGE_NEEDED`.
- `/api/demo/scenario/protein-distribution`: produced `PROTEIN_DISTRIBUTION_CONFLICT`.
- `/api/demo/scenario/high-protein-hydration`: produced `HIGH_PROTEIN_HYDRATION_NEEDED`.
- `/api/demo/scenario/long-fast`: produced `LONG_FAST_DETECTED` and `LongFastDetected`.
- `/api/demo/scenario/activity-weight`: recalculated PAL to `1.725` and produced a weight oscillation recommendation.

## Specification Coverage Checklist

Implemented from the PDF:

- Static user profile: gender, age, weight, height, goal, PAL/activity factor, allergies, targets.
- BMR and TDEE calculation using Mifflin-St Jeor.
- Protein and fiber target initialization.
- Dynamic meal input with calories, protein, carbohydrates, fat, fiber, sugars, category, timestamp, and user id.
- Primitive events folder with `MealLogged`, `WeightMeasured`, `NewActivityRoutine`, and `MealSkipped`.
- Daily intake aggregation from meal events.
- Weekly pattern/trend facts.
- Analytic statuses and recommendations.
- Rule templates for calorie balance and temporal pattern thresholds.
- Forward chaining rules for calorie surplus, deficit, maintenance, conflicts, protein warnings, protein floor, and hydration.
- CEP rules for late-night eating, breakfast skipping, binge pattern, unplanned snacking, protein deficit, weight stagnation, calorie spike, and long fast.
- Chained reasoning for overeating, muscle-loss/protein-critical risk, and plan-change/adaptation.
- Backward-style explanation queries for overeating, protein critical, and plan change.
- Frontend-compatible JSON endpoint: `POST /api/nutrition/evaluate`.

## Diagram

The class diagram is available at:

- `class-diagram.png`

It shows the relationship between users, meals, daily/weekly aggregates, analytic statuses, recommendations, primitive events, CEP pattern facts, and template parameters.
