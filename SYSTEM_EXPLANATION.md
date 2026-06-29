# Adaptive Nutrition Expert System - Complete System Explanation

## System Overview

The Adaptive Nutrition Expert System is a rule-based intelligent application that uses **Complex Event Processing (CEP)** and **Drools Rules Engine** to detect nutritional patterns, track dietary intake, and provide real-time recommendations for users.

The system architecture consists of three main components:

1. **Frontend (Angular)**: Interactive dashboard for meal logging and monitoring
2. **Backend (Spring Boot)**: REST API that orchestrates the Drools rules engine
3. **Rules Engine (Drools/Kie)**: Stateful session that processes facts and fires rules to detect patterns

---

## How the System Works

### 1. Data Flow Architecture

```
User Input (Meals, Goals, Weight)
         ↓
Angular Frontend (Dashboard)
         ↓
Spring Boot REST API
         ↓
Drools Rules Engine (KieSession)
         ↓
Pattern Detection & Recommendations
         ↓
Response returned to Frontend
         ↓
User sees Analysis & Suggestions
```

### 2. User Profile & Goal Setup

Users define their nutrition goals through the **"Goals"** tab:

- **Personal Information**: Gender, age, weight, height
- **Activity Level**: Sedentary, Light, Moderate, Very Active (1.2 - 1.725 multiplier)
- **Fitness Goal**: Weight Loss, Maintenance, or Muscle Gain
- **BMR Calculation**: Basal Metabolic Rate (Harris-Benedict equation)

Example calculation:
```
BMR = 1664 + (9.6 × weight) + (1.8 × height) - (4.7 × age)  [Female]

TDEE = BMR × Activity Factor ± Goal Adjustment
- Weight Loss: TDEE - 500 kcal
- Maintenance: TDEE + 0 kcal
- Muscle Gain: TDEE + 300 kcal

Macros:
- Protein: Weight × 1.0-1.8g (depending on goal)
- Fiber: 25g (Female) / 38g (Male)
```

### 3. Daily Meal Logging

Users log meals with nutritional information:

- **Meal Name, Category** (Breakfast, Lunch, Dinner, Snack)
- **Timestamp**: Exact time the meal was consumed
- **Macronutrients**: Calories, Protein (g), Carbs (g), Fat (g), Fiber (g), Sugars (g)
- **Breakfast Skipping Indicator**: Track missed breakfast events

### 4. Rule-Based Analysis

The system uses **Drools** (open-source rule engine) organized in several rule files:

#### a) **nutrition-rules.drl** - Core Logic
- **Initialize user targets**: Sets up calorie/macro targets based on profile
- **Calorie status detection**: SURPLUS, DEFICIT, or MAINTENANCE_STABLE
- **Pattern recognition**: Detects eating patterns over time
- **Recommendations**: Generates personalized advice based on detected issues
- **Weight tracking**: Analyzes weight trends and adjusts recommendations

#### b) **calorie-balance.drl** - Generated from template
Uses Drools templates to dynamically generate rules:
```
Rule: "Rule_Calorie_Surplus"
When: User target exists AND daily calories > 110% of target
Then: Set status to SURPLUS and alert user

Rule: "Rule_Calorie_Deficit"
When: User target exists AND daily calories < 70% of target
Then: Set status to DEFICIT (for weight loss goal)

Rule: "Rule_Maintenance_Stability"
When: Calories within 95-105% of target
Then: Set status to MAINTENANCE_STABLE
```

#### c) **temporal-patterns.drl** - Generated from template
Detects patterns over time using **CEP windows**:

```
Rule: "Template_LateNightEatingPattern"
When: 
  - User exists
  - Count >= 3 meals between 22:00-06:00 (over last 7 days)
  - Pattern not already detected
Then: Insert LateNightEatingPattern fact

Rule: "Template_BreakfastSkippingPattern"
When:
  - User exists
  - Count >= 3 skipped breakfasts (over last 3 days)
  - Pattern not already detected
Then: Insert BreakfastSkippingPattern fact

Rule: "Template_BingePattern"
When:
  - User exists
  - Count >= 3 meals within 90 minutes
  - Pattern not already detected
Then: Insert BingePattern fact
```

### 5. Complex Event Processing (CEP)

**CEP** detects temporal patterns across a stream of events.

**Key Concept**: Instead of just looking at today's data, CEP watches events over time windows:

- **Time Windows**: "Over the last 7 days", "in the last 3 hours", etc.
- **Event Correlation**: Links related events (e.g., "3 late meals = pattern")
- **Stateful Detection**: Remembers past patterns to avoid duplicate alerts

**Example CEP in Action**:
```
Day 1 at 23:00 → User eats meal #1 (late)
Day 2 at 22:30 → User eats meal #2 (late)
Day 3 at 23:15 → User eats meal #3 (late)

[CEP processes accumulated events]

Result: LateNightEatingPattern DETECTED!
Recommendation: "Consider eating earlier. Late meals can disrupt sleep and metabolism."
```

**What "Seed Expert Scenario" Does**:
The button simulates a realistic scenario for testing:
- Skips breakfast for 3 consecutive days
- Adds late-night high-calorie dinners each day
- System immediately detects the pattern and generates appropriate recommendations

This is useful for **demonstration without waiting 7 days** to see pattern detection work.

---

## System Components in Detail

### Frontend Architecture (Angular)

**File**: `frontend/src/app/app.component.ts`

**State Management**:
```typescript
interface AppState {
  user: UserProfile              // Current user goals/metrics
  journals: Record<string, DayJournal>  // Meals by date
  weightMeasurements: WeightMeasured[]  // Weight tracking
  evaluations: Record<string, NutritionEvaluationResponse>  // Rule results
}
```

**Dashboard Layout** (Reorganized):
1. **Expert Opinion Card** (Left, Top): Shows status + calorie progress + recommendations
2. **Today Card** (Right, Top): Daily macro summary + evaluation button
3. **Add Food Event Card** (Left, Middle): Meal input form + test scenarios
4. **Logged Meals Card** (Right, Middle): List of meals for selected day
5. **Weekly Pattern Card** (Full width, Bottom): 7-day trends + seed/clear buttons

**Test Scenario Buttons** (Quick testing):
- **Late Night Binge**: Skipped breakfast + late dinner + snack at 23:50
- **Breakfast Skipping**: Breakfast skipped, lunch & dinner, mid-morning snack
- **Eating Less Than Goal**: Small breakfast/lunch/dinner totaling ~900 kcal
- **High Protein Day**: 5 meals optimized for protein (muscle gain goal)

### Backend Architecture (Spring Boot)

**File**: `service/src/main/java/.../NutritionEvaluationController.java`

```java
@PostMapping("/api/nutrition/evaluate")
public NutritionEvaluationResponse evaluate(@RequestBody NutritionEvaluationRequest request) {
    // 1. Create stateful KieSession from KieContainer
    KieSession session = kieContainer.newKieSession("nutritionKSession");
    
    // 2. Insert facts (User, DailyIntake, Meals, WeeklyPattern, etc.)
    session.insert(user);
    session.insert(dailyIntake);
    session.insert(meals);
    session.insert(weeklyPattern);
    
    // 3. Fire all rules (CEP processes events)
    int firedRules = session.fireAllRules();
    
    // 4. Extract results from session memory
    analyticStatuses = session.getObjects(AnalyticStatus.class)
    recommendations = session.getObjects(Recommendation.class)
    patterns = session.getObjects(NutritionPattern.class)
    
    // 5. Return response
    return NutritionEvaluationResponse.builder()
        .dailyStatus(dailyIntake.getStatus())
        .recommendations(recommendations)
        .detectedPatterns(patterns)
        .firedRules(firedRules)
        .build();
}
```

**Why Backend Returns Zeros Initially**:
The response populates fields from the modified facts after rule firing. If rules don't fire correctly:
1. DailyIntake object isn't modified → status remains null
2. No DailyIntake facts → totalCalories, totalProtein stay 0
3. No patterns match → detectedPatterns is empty

**Fix**: Ensure CSV templates have correct data columns and template rules are properly generated.

### Drools Rules Engine

**File**: `kjar/src/main/resources/rules/nutrition-rules.drl`

**Key Mechanism**: Rules fire in sequence based on **salience** (priority):

```
rule "Initialize user target values"
  salience 100  ← Fires FIRST (highest priority)
when
  $user : User(targetCalories == 0 || targetProtein == 0)
then
  modify($user) { initializeTargets() }  ← Sets up targets
end

rule "Calorie status detection"
  salience 50
when
  $user : User($target : targetCalories > 0)
  $intake : DailyIntake(userId == $user.id)
  eval($intake.getTotalCalories() > $target * 1.10)
then
  modify($intake) { setStatus(SURPLUS) }  ← Updates status
end
```

**Drools Template System** (Generates Rules Dynamically):

Templates allow generating rules without hardcoding. Example:

**Template File** (`calorie-balance.drt`):
```
template header
ruleName, operator, percentage, resultingStatus

rule "@{ruleName}"
when
  $user : User($userId : id, $target : targetCalories > 0)
  $intake : DailyIntake(userId == $userId)
  eval($intake.getTotalCalories() @{operator} ($target * @{percentage}))
then
  modify($intake) { setStatus(DailyIntakeStatus.@{resultingStatus}) }
end
```

**Data File** (`calorie-balance.csv`):
```
ruleName, operator, percentage, resultingStatus
Rule_Calorie_Surplus, >, 1.10, SURPLUS
Rule_Calorie_Deficit, <, 0.70, DEFICIT
```

**Generated Output** (`calorie-balance.drl`):
```
rule "Rule_Calorie_Surplus"
when
  $user : User($userId : id, $target : targetCalories > 0)
  $intake : DailyIntake(userId == $userId)
  eval($intake.getTotalCalories() > ($target * 1.10))
then
  modify($intake) { setStatus(DailyIntakeStatus.SURPLUS) }
end
```

---

## Pattern Detection Deep Dive

### 1. Late Night Eating Pattern

**Rule**:
```
Count >= 3 meals between 22:00-06:00 in last 7 days
```

**Why It Matters**:
- Disrupts circadian rhythm
- Metabolic rate is lower at night
- Can lead to weight gain and sleep issues

**Recommendation Generated**:
"Late-night eating detected. Consider eating your last meal by 8 PM to support better sleep and metabolism."

### 2. Breakfast Skipping Pattern

**Rule**:
```
Count >= 3 skipped breakfasts in last 3 days
```

**Why It Matters**:
- Breakfast kickstarts metabolism
- Skipping breakfast → overeating later (binge compensation)
- Associated with lower nutrient intake

**Recommendation Generated**:
"Breakfast is frequently skipped. Eating breakfast improves energy, focus, and metabolic health."

### 3. Binge Pattern

**Rule**:
```
Count >= 3 meals within 90 minutes
```

**Why It Matters**:
- Rapid eating → poor satiety signals
- Digestive stress
- Often followed by guilt and restriction

**Recommendation Generated**:
"Rapid meal consumption detected. Try eating slowly and mindfully to improve digestion and satisfaction."

### 4. Calorie Deficit/Surplus Detection

**Rules**:
```
Deficit: Daily calories < 70% of target (for weight loss goal)
Surplus: Daily calories > 110% of target
Maintenance: 95-105% of target
```

**Used For**:
- Goal progress tracking
- Adjusting recommendations
- Preventing extreme behavior

---

## Pseudo Clock / Demo Mode

**Why We Need It**:
Real CEP requires waiting days/weeks for patterns to emerge. In defense presentation, time is limited.

**Solution**: Drools supports a **Pseudo Clock** mode:

```java
// Instead of real time
KieSession session = kieContainer.newKieSession("nutritionKSession");
session.setGlobal("drools.clockType", "pseudo");

// We control time advancement
SessionPseudoClock pseudoClock = session.getSessionClock();

// Skip forward 7 days
pseudoClock.advanceTime(7, TimeUnit.DAYS);

// Rule with "over window:time(7d)" now triggers immediately
session.fireAllRules();
```

**For Presentation**:
1. Load test scenario (late night binge meals across multiple days)
2. If real CEP fails, switch to pseudo clock
3. Demonstrate pattern detection with fast-forwarded time
4. Show recommendations changing based on detected patterns

---

## Key Facts & Objects

### User
```java
public class User {
  String id;
  String name;
  Gender gender;           // MALE, FEMALE
  int age;
  double weight;          // kg
  double height;          // cm
  GoalType goal;          // WEIGHT_LOSS, MAINTENANCE, MUSCLE_GAIN
  double activityFactor;  // 1.2 - 1.725
  double targetCalories;  // Auto-calculated
  double targetProtein;   // Auto-calculated
  double targetFiber;     // Auto-calculated
  double bmr;             // Auto-calculated
}
```

### DailyIntake
```java
public class DailyIntake {
  String date;
  String userId;
  double totalCalories;
  double totalProtein;
  double totalFiber;
  int mealCount;
  DailyIntakeStatus status;  // SURPLUS, DEFICIT, MAINTENANCE_STABLE
}
```

### Meal Event
```java
public class Meal {
  String userId;
  String name;
  LocalDateTime timestamp;
  MealCategory category;     // BREAKFAST, LUNCH, DINNER, SNACK
  double calories;
  double protein;
  double carbohydrates;
  double fat;
  double fiber;
  double sugars;
}
```

### Detected Patterns (CEP Output)
```java
public class LateNightEatingPattern extends NutritionPattern {
  String userId;
  LocalDate detectedDate;
  int frequency;  // How many late meals
}

public class BreakfastSkippingPattern extends NutritionPattern {
  String userId;
  LocalDate detectedDate;
  int consecutiveSkips;
}

public class BingePattern extends NutritionPattern {
  String userId;
  LocalDate detectedDate;
  int eventCount;      // # of rapid meals
  int windowMinutes;   // Time window (90)
}
```

### Recommendations
```java
public class Recommendation {
  String userId;
  String message;
  String type;           // e.g., "LATE_NIGHT_EATING", "BREAKFAST_SKIPPING"
  SeverityLevel severity;  // LOW, MEDIUM, HIGH
}
```

---

## Workflow: Complete Example

### User Scenario: Weight Loss Goal

1. **Setup** (Goals Tab):
   - Female, 30 years, 75kg, 165cm
   - Goal: WEIGHT_LOSS
   - Activity: Moderate (1.55)
   
2. **Calculations**:
   - BMR = 1664 + (9.6 × 75) + (1.8 × 165) - (4.7 × 30) ≈ 1500 kcal
   - TDEE = 1500 × 1.55 ≈ 2325 kcal
   - Target = 2325 - 500 = **1825 kcal/day**
   - Protein Target = 75 × 1.4 = **105g/day**
   - Fiber Target = **25g/day**

3. **Logging** (Dashboard Tab):
   - Day 1: Logs breakfast (350), lunch (500), skips dinner
   - Total: 850 kcal (46% of target) → DEFICIT status
   
4. **Rules Fire**:
   - "Initialize user target values" → Sets 1825 kcal target
   - "Calorie status detection" → 850 < (1825 × 0.70) → **DEFICIT**
   - "Recommendation for deficit" → "Your intake is significantly below target. Consider adding nutritious calories for sustainable weight loss."

5. **CEP Detection** (Over 3 days):
   - Day 1: Skips dinner
   - Day 2: Skips breakfast and dinner
   - Day 3: Skips breakfast
   - Count >= 3 → **BreakfastSkippingPattern detected**
   - Recommendation: "Don't skip breakfast-it's essential for energy and preventing overeating later."

6. **Weekly Summary**:
   - Shows all patterns detected this week
   - Avg daily intake: 1200 kcal (losing ~1 lb/week)
   - Recommendations synthesized from all rule fires

---

## Testing & Presentation Strategy

### Quick Test Scenarios
Click buttons to instantly populate realistic scenarios:
- **Late Night Binge**: See late eating pattern within seconds
- **Breakfast Skipping**: See breakfast pattern detection
- **Under-eating**: See deficit handling
- **High Protein**: See macro tracking

### Seed Expert Scenario
Simulates 3 days of late dinner + skipped breakfast → Pattern detected

### Clear Selected Day
Reset to test fresh scenarios

### Evaluate Button
Manually re-evaluate after changes (auto-runs on every change)

---

## Summary: The Three Layers

| Layer | Tech | Purpose |
|-------|------|---------|
| **Presentation** | Angular | User interface, meal logging, visualization |
| **API** | Spring Boot | Request handling, session management, rule orchestration |
| **Intelligence** | Drools + CEP | Pattern detection, rule evaluation, recommendations |

**Data Flow**:
User Input → Frontend → Backend API → Drools KieSession → CEP Pattern Detection → Recommendations → Response → Frontend Display

**Key Innovation**:
Combines traditional rule-based expert systems with **Complex Event Processing** to detect temporal patterns that static rules can't catch.

---

## For Defense Presentation

**What to Emphasize**:
1. The system detects multiple patterns using **CEP** (not just static rules)
2. Recommendations are **personalized** based on user goals
3. Time-windows allow pattern detection across **multiple days**
4. Template-driven rules make it easy to **add new patterns** (no code change needed)
5. Real-time evaluation provides **immediate feedback** on dietary choices

**Demo Flow**:
1. Set up user profile (show TDEE calculation)
2. Log test scenario (show different meals)
3. Click "Evaluate" (show rules firing, patterns detected, recommendations)
4. Seed expert scenario (show CEP detecting 3-day pattern)
5. Switch between days (show weekly pattern detection)

---

## Troubleshooting

### Backend Returns All Zeros
- **Cause**: Rules not firing / DailyIntake not being modified
- **Check**: Log file shows "ERR" in generated-template-rules.drl parsing
- **Fix**: Verify CSV template files have correct columns

### No Patterns Detected
- **Cause**: CEP window conditions not met
- **Check**: Do you have 3+ events in the time window?
- **Fix**: Use "Seed Expert Scenario" button for instant 3-day pattern

### Pseudo Clock Not Working
- **Cause**: Drools version mismatch
- **Config**: Edit kmodule.xml clockType="pseudo" instead of "realtime"
- **Result**: `advanceTime()` will work for demo purposes

