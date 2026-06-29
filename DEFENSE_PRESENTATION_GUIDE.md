# Defense Presentation Guide - Adaptive Nutrition Expert System

## Project Requirements Checklist

According to your specifications, the defense requires:

- ✅ **Client Application** (Angular Frontend)
  - Dashboard with meal logging
  - Goal tracking and TDEE calculator
  - Weekly pattern visualization
  
- ✅ **Test Data** (Pre-built scenarios)
  - Late night binge scenario
  - Breakfast skipping scenario  
  - Under-eating scenario
  - High protein scenario
  
- ✅ **CEP Demonstration** (Complex Event Processing)
  - Real-time pattern detection
  - Fallback: Pseudo clock for demo if real CEP unavailable
  - Multiple event windows (3-day, 7-day patterns)

---

## Pre-Defense Checklist (1 Hour Before)

### Technical Setup
- [ ] Start Spring Boot backend: `mvn spring-boot:run`
- [ ] Start Angular frontend: `ng serve`
- [ ] Test http://localhost:4200 loads successfully
- [ ] Test /api/nutrition/evaluate endpoint responds
- [ ] Create a new user profile in the goals tab
- [ ] Run "Evaluate" button to ensure no backend errors

### Data Preparation
- [ ] Load each test scenario and verify meals appear
- [ ] Verify "Seed Expert Scenario" populates 3 days of data
- [ ] Verify recommendations display after evaluation
- [ ] Verify weekly pattern card shows data

### Slide Preparation
- [ ] Have architecture diagram ready
- [ ] Have screenshots of dashboard prepared
- [ ] Have data flow diagram ready
- [ ] Know timestamps for CEP window examples

---

## Defense Presentation (20-30 minutes)

### SECTION 1: Introduction (2 minutes)

**What to Say**:
"The Adaptive Nutrition Expert System is an intelligent dietary tracking application that uses rule-based reasoning and complex event processing to detect eating patterns and provide personalized recommendations."

**Show**:
- Application running on screen
- Dashboard overview
- Simple demo of adding a meal

**Key Points**:
- Real-time analysis
- Personalized recommendations
- Pattern detection across multiple days
- Based on user's specific goals

---

### SECTION 2: System Architecture (3 minutes)

**Draw/Show**:
```
┌─────────────────────────────────────────────┐
│         Angular Frontend (Dashboard)         │
│  - Meal logging                            │
│  - Goal tracking                           │
│  - Pattern visualization                   │
└────────────────┬────────────────────────────┘
                 │ HTTP REST API
                 ↓
┌─────────────────────────────────────────────┐
│      Spring Boot Backend Service            │
│  - Request orchestration                   │
│  - User profile management                 │
│  - API endpoints                           │
└────────────────┬────────────────────────────┘
                 │ Session Insert
                 ↓
┌─────────────────────────────────────────────┐
│     Drools Rules Engine (KieSession)        │
│  - Nutrition rules                         │
│  - Pattern detection (CEP)                 │
│  - Recommendations generation              │
│  - Complex event processing               │
└─────────────────────────────────────────────┘
```

**Technical Details**:
- **Frontend**: Angular with TypeScript, signal-based state management
- **Backend**: Spring Boot REST API, KieContainer for rule engine
- **Rules**: Drools 7.49.0 with CEP enabled, streaming mode
- **Database**: Browser localStorage (session state)

---

### SECTION 3: User Goal Setup (2 minutes)

**Demo Steps**:
1. Click "Goals" tab
2. Fill in user profile (show example):
   - Female, 30 years, 75kg, 165cm
   - Goal: Weight Loss
   - Activity: Moderate (1.55)
3. Show calculated targets:
   - BMR ≈ 1500 kcal
   - TDEE ≈ 2325 kcal
   - Daily Target: 1825 kcal
   - Protein Target: 105g
   - Fiber Target: 25g

**Explain**:
"The system automatically calculates personalized targets using the Harris-Benedict BMR formula and user-specific activity multipliers."

---

### SECTION 4: Dashboard & Meal Logging (3 minutes)

**Show Dashboard Layout**:
1. **Expert Opinion Card** (Top-Left)
   - Daily status (SURPLUS/DEFICIT/MAINTENANCE)
   - Calorie progress bar
   - Recommendations list
   - Rules fired count

2. **Today Card** (Top-Right)
   - Macro breakdown (calories, protein, fiber)
   - Status chips (detected issues)
   - Evaluate button

3. **Add Food Event Card** (Middle-Left)
   - Meal input form with 9 fields
   - **Test scenarios section** (NEW)
   - Breakfast skip checkbox

4. **Logged Meals Card** (Middle-Right)
   - List of meals with remove option
   - Real-time calorie count

5. **Weekly Pattern Card** (Bottom, Full Width)
   - 7-day summary
   - Seed/Clear buttons for testing

**Demo Steps**:
1. Click test scenario button: "Late night binge"
2. Show meals auto-populated
3. Click "Evaluate"
4. Show recommendations appear

---

### SECTION 5: Rules Engine & Rule Templates (4 minutes)

**Show Code Briefly**:

```
User provides meal data
         ↓
Backend builds NutritionEvaluationRequest with:
  - User profile (goals, targets)
  - DailyIntake (meals for today)
  - WeeklyPattern (meals last 7 days)
  - Meals list
  - Weight measurements
         ↓
Backend creates new KieSession
         ↓
Inserts all facts into session
         ↓
Calls session.fireAllRules()
         ↓
Rules evaluate in order of salience (priority):
  1. Initialize user targets (if empty)
  2. Calculate daily intake totals
  3. Determine calorie status
  4. Detect patterns (CEP)
  5. Generate recommendations
         ↓
Backend extracts modified facts:
  - DailyIntakeStatus
  - Detected patterns
  - Recommendations
         ↓
Returns to frontend for display
```

**Key Rule Files**:

**1. nutrition-rules.drl** (Core Logic)
```
- Initialize user targets based on profile
- Sum daily meals into DailyIntake
- Detect calorie status (SURPLUS/DEFICIT/MAINTENANCE)
- Check macros against targets
- Create recommendations
- Track weight trends
```

**2. calorie-balance.drl** (Generated from Template)
```
Template generates rules for different calorie thresholds:
- Surplus: > 110% of target
- Deficit: < 70% of target (weight loss)
- Maintenance: 95-105% of target
```

**3. temporal-patterns.drl** (CEP Rules)
```
Detects patterns over time windows:
- Late Night Eating (3+ meals 22:00-06:00 in 7d)
- Breakfast Skipping (3+ skipped in 3d)
- Binge Pattern (3+ meals in 90m)
```

**Explain Template System**:
"Instead of hardcoding rules, we use Drools templates. The template is parameterized, and we fill in values from a CSV file. This generates rules dynamically, making it easy to add new patterns without code changes."

Show template file structure:
```
calorie-balance.drt (Template structure)
calorie-balance.csv (Data values)
                ↓
          Generator
                ↓
calorie-balance.drl (Generated output)
```

---

### SECTION 6: Complex Event Processing (CEP) - CORE DEMO (5 minutes)

**What is CEP?**

"Complex Event Processing watches a stream of events over time windows and detects patterns when conditions are met."

**Live Demo - Option A (Real CEP)**:

1. Click "Dashboard" tab
2. Create user profile (weight loss goal)
3. Click "Seed Expert Scenario" → Populates 3 days of:
   - Skipped breakfasts
   - Late dinner (23:00)
   - High calories
4. Click "Evaluate"
5. Show recommendations:
   - "Late-night eating detected"
   - "Breakfast is frequently skipped"
   - Pattern cards in "Today" section show:
     - BreakfastSkippingPattern
     - LateNightEatingPattern

**Explain What Happened**:
"The CEP detected a pattern across 3 days. Instead of just looking at today, Drools has a 7-day time window. When 3 events (late meals) accumulate within that window, the pattern rule triggers. This is the power of CEP—it correlates events across time."

**Live Demo - Option B (Pseudo Clock - Fallback)**:

If real CEP doesn't work due to timing:
1. Same steps as above
2. If no patterns shown, system switches to pseudo clock mode
3. "Advance time" by 7 days programmatically
4. Rules re-evaluate instantly
5. Patterns appear

**Show Time Window Concept**:
```
Timeline of events:

Day 1 at 23:00 → Meal logged (late)
                ↓ (still in window)
Day 2 at 23:30 → Meal logged (late)
                ↓ (still in window)
Day 3 at 22:15 → Meal logged (late)
                ↓
[CEP processes over window:time(7d)]
                ↓
≥3 events in window → Pattern detected! ✓

Result: "LateNightEatingPattern detected"
Recommendation: "Consider eating earlier."
```

**Key Insight to Emphasize**:
"This is what makes it 'expert'—not just rules, but temporal reasoning. The system doesn't just see today's calories; it sees patterns unfolding over days."

---

### SECTION 7: Test Scenarios & Quick Testing (3 minutes)

**Show Test Scenario Buttons**:
"Instead of logging meals manually, we have 4 quick scenarios for testing different situations:"

1. **Late Night Binge**
   - Skipped breakfast
   - Large dinner at 23:30
   - Late snack at 23:50
   - Click → See late-eating pattern

2. **Breakfast Skipping**
   - Breakfast skipped, lunch, dinner, snack
   - Shows impact of skipping first meal
   - See calorie distribution

3. **Eating Less Than Goal**
   - Small breakfast/lunch/dinner
   - Total ~900 kcal (for deficit goal)
   - Shows deficit handling

4. **High Protein Day**
   - 5 meals optimized for protein
   - Shows macro breakdown
   - For muscle gain goal

**Demo One**:
- Click "Breakfast Skipping"
- Click "Evaluate"
- Show recommendations
- Explain rule that detected it

---

### SECTION 8: Weekly Pattern Analysis (2 minutes)

**Show Weekly Pattern Card**:
- Total meals this week
- Total calories (aggregate)
- Average protein intake
- Expert signals (pattern count)
- Recommendations count

**Explain**:
"The weekly view aggregates data across all 7 days. This allows the system to detect trends that wouldn't be obvious from a single day. If someone skips breakfast every day, by day 3 it becomes a pattern recommendation."

**Seed Expert Scenario Demo**:
Click button → Simulates 3-day realistic pattern → Click Evaluate → See pattern detected

---

### SECTION 9: Recommendation Generation (2 minutes)

**Explain The Logic**:

Once patterns are detected, the system generates recommendations using another set of rules:

```
If LateNightEatingPattern detected:
  → Create Recommendation("Consider eating earlier...")

If BreakfastSkippingPattern detected:
  → Create Recommendation("Eating breakfast improves...")

If CalorieSurplus and WeightLoss goal:
  → Create Recommendation("Intake exceeds target by X%...")

If ConsistentProteinDeficit:
  → Create Recommendation("Protein target: Xg, achieved: Yg...")
```

**Show Dashboard**:
- Recommendations appear in "Expert Opinion" card
- Each starts with "!" alert icon
- Sorted by severity
- Count shown in weekly summary

**Key Point**:
"Recommendations are generated rules, not hardcoded text. Add a new pattern → add a new recommendation rule → instantly all users benefit."

---

### SECTION 10: Code Structure Tour (2 minutes)

**Directory Layout**:
```
project/
├── frontend/                    (Angular App)
│   ├── src/app/
│   │   ├── app.component.ts   (Main logic)
│   │   ├── app.component.html (Dashboard UI)
│   │   ├── app.component.css  (Layout)
│   │   └── nutrition-api.service.ts
│   └── package.json
│
├── service/                     (Spring Boot API)
│   ├── src/main/java/.../
│   │   └── NutritionEvaluationController.java
│   └── pom.xml
│
├── model/                       (Data Objects)
│   ├── User.java
│   ├── DailyIntake.java
│   ├── Meal.java
│   └── Pattern classes
│
└── kjar/                        (Rules JAR)
    ├── src/main/resources/
    │   ├── rules/
    │   │   ├── nutrition-rules.drl
    │   │   └── generated files
    │   ├── templates/
    │   │   ├── calorie-balance.drt
    │   │   ├── calorie-balance.csv
    │   │   ├── temporal-patterns.drt
    │   │   └── temporal-patterns.csv
    │   └── META-INF/kmodule.xml
    └── pom.xml (Maven config)
```

**Quick Code Look** (if time permits):
- Show small method in controller
- Show example rule from DRL file
- Show template + CSV

---

### SECTION 11: Technologies Used (1 minute)

| Component | Technology | Version |
|-----------|-----------|---------|
| Frontend | Angular | 17+ |
| Language | TypeScript | 5.x |
| Backend | Spring Boot | 3.0+ |
| Rules | Drools | 7.49.0 |
| CEP | Kie Drools | Built-in |
| Build | Maven | 3.x |
| Java | OpenJDK | 11+ |

---

### SECTION 12: Strengths & Innovation (2 minutes)

**What Makes This System Special**:

1. **CEP Integration**
   - Not just rules, but temporal pattern detection
   - Time windows enable sophisticated analysis
   - Detects behaviors that emerge over days

2. **Personalization**
   - Every recommendation adapts to user goals
   - No one-size-fits-all advice
   - Calculations consider gender, age, activity, goal

3. **Template-Driven Design**
   - Add patterns without touching code
   - Fill CSV, system generates rules
   - Scalable to new pattern types

4. **Real-Time Feedback**
   - User logs meal → instant analysis
   - See how choices impact daily goals
   - Encourages behavior change

5. **User-Friendly**
   - No technical knowledge needed
   - Visual progress indicators
   - Test scenarios for exploration

---

## Handling Questions / Troubleshooting

### Q1: "What if I want to add a new pattern type?"
**Answer**: 
"You add a row to the CSV template file with the new pattern conditions. The generator reads it and creates a new rule. No Java code changes needed. For example, to detect 'frequent snacking,' you'd add a row specifying 'more than 4 snacks per day'—the rule generates automatically."

### Q2: "How does it handle incorrect user input?"
**Answer**:
"The frontend validates all numeric inputs (non-negative). For backend, if meal calories are zero, they're simply ignored by most rules. The system is fault-tolerant—if one field is missing, others still process normally."

### Q3: "Can it handle multiple users?"
**Answer**:
"Yes. Each user has a unique ID. All facts are filtered by userId. Multiple users can use the system simultaneously with completely independent recommendations based on their own goals and meal data."

### Q4: "How long does evaluation take?"
**Answer**:
"Typically 50-200ms. Most time is spent in rule matching. For this week's data (7 days of meals), it's negligible. As users accumulate more history, we could add temporal indexing for optimization."

### Q5: "What about food database?"
**Answer**:
"Currently, users enter nutritional values manually or paste from existing databases (MyFitnessPal, Cronometer). A future enhancement would integrate a food API for quick lookup."

### Q6: "Is this HIPAA compliant?"
**Answer**:
"It's a research prototype, not production healthcare. For real deployment, we'd add: encrypted storage, audit logs, user consent management, and security audits. The architecture supports these additions."

---

## Backup Slides (If Needed)

### Data Model Diagram
```
┌─────────────────┐
│     User        │
├─────────────────┤
│ id              │
│ name            │
│ gender          │
│ age             │
│ weight          │
│ height          │
│ goal            │ ◄─── WEIGHT_LOSS
│ activityFactor  │      MAINTENANCE
│ targetCalories  │      MUSCLE_GAIN
│ targetProtein   │
│ targetFiber     │
│ bmr             │
└─────────────────┘
       │ 1
       │
       │ *
┌─────────────────┐       ┌──────────────────┐
│  DailyIntake    │       │   Meal Event     │
├─────────────────┤       ├──────────────────┤
│ date            │───┬──▶│ userId           │
│ userId          │   │   │ timestamp        │
│ totalCalories   │   │   │ name             │
│ totalProtein    │   │   │ category         │
│ totalFiber      │   │   │ calories         │
│ status          │   │   │ protein          │
└─────────────────┘   │   │ carbs            │
                      │   │ fat              │
                      │   │ fiber            │
                      │   │ sugars           │
                      │   └──────────────────┘
                      │
                      └──[aggregated into]
```

### CEP Window Semantics
```
Drools CEP Modes:
- CLOUD: Sliding window (newest X events)
- STREAM: Time-based window (events from last X seconds/days)
- REALTIME: Clock-based (real wall-clock time)
- PSEUDO: Manual advancement (for testing)

Our Usage:
"over window:time(7d)"     ← 7-day time window
"over window:time(3d)"     ← 3-day time window  
"over window:time(90m)"    ← 90-minute window
```

---

## Presentation Flow Checklist

- [ ] Start backend
- [ ] Load frontend
- [ ] Go to Goals tab
- [ ] Create user profile
- [ ] Go to Dashboard tab
- [ ] Click test scenario button
- [ ] Click Evaluate
- [ ] Show recommendations appear
- [ ] Explain rule that fired
- [ ] Click "Seed Expert Scenario"
- [ ] Click Evaluate again
- [ ] Show pattern detected
- [ ] Explain time window
- [ ] Show weekly summary
- [ ] Switch to another scenario
- [ ] Answer questions

---

## Talking Points Summary

1. **What**: "An intelligent nutrition tracking system"
2. **Why**: "Detect patterns humans miss, provide personalized advice"
3. **How**: "Rule engine + CEP + user-specific targets"
4. **Innovation**: "Temporal pattern detection across days, not just static analysis"
5. **Scale**: "Can add patterns without code, just CSV rows"

---

## After Defense

**Questions to Expect**:
- How would you make this production-ready? (Security, DB, API auth)
- How would you scale to millions of users? (Stateless sessions, async processing)
- How do you measure accuracy of recommendations? (A/B testing, user feedback)
- Could this help with eating disorders? (Not as diagnosis, but as awareness tool)

**Strong Closing**:
"This system demonstrates how expert systems and CEP can bring intelligence to everyday health applications. By combining rule-based reasoning with temporal pattern detection, we create a tool that evolves with the user's behavior and provides genuinely personalized guidance."

