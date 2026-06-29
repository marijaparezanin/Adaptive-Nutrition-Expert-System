# Test Data Guide - Quick Scenarios

## Overview

The application includes 4 pre-built test scenarios accessible via buttons on the **"Add Food Event"** card. These scenarios instantly populate meals for the selected day to demonstrate pattern detection without manual data entry.

Each scenario is designed to trigger specific patterns and recommendations.

---

## Test Scenarios

### 1. Late Night Binge

**Button**: "Late night binge"

**What It Simulates**:
- Skipped breakfast (morning is empty)
- Large late dinner at 23:30 (1200 kcal)
- Late-night snack at 23:50 (400 kcal)

**Meal Data**:
```
Breakfast: SKIPPED

Dinner (23:30)
- Name: Large late dinner
- Calories: 1200
- Protein: 35g
- Carbs: 150g
- Fat: 45g
- Fiber: 8g
- Sugars: 40g

Snack (23:50)
- Name: Late night snack
- Calories: 400
- Protein: 8g
- Carbs: 55g
- Fat: 16g
- Fiber: 2g
- Sugars: 35g

Daily Total: 1600 kcal
```

**Total Daily Intake**: 1600 kcal
**Timestamp Range**: 23:30 - 23:50 (20-minute window)

**Patterns Triggered** (After Evaluate):
- ✓ **LateNightEatingPattern**: 2 meals between 22:00-06:00
- ✓ **BreakfastSkippingPattern**: Breakfast skipped (if seeded over 3+ days)
- ✓ **BingePattern**: 2 meals within 90 minutes

**Recommendations Generated**:
- "Late-night eating detected. Consider eating your last meal by 8 PM."
- "Rapid meal consumption detected. Try eating slowly."
- "Breakfast is frequently skipped. Eating early improves energy and focus."

**Use Case**: 
- Demonstrate late-eating pattern detection
- Show time-window based CEP (meals 22:00-06:00)
- Show multiple patterns from single scenario

---

### 2. Breakfast Skipping

**Button**: "Breakfast skipping"

**What It Simulates**:
- Skipped breakfast
- Mid-morning snack to compensate
- Regular lunch and dinner

**Meal Data**:
```
Breakfast: SKIPPED

Snack (10:30)
- Name: Mid-morning snack
- Calories: 200
- Protein: 6g
- Carbs: 25g
- Fat: 8g
- Fiber: 3g
- Sugars: 12g

Lunch (12:45)
- Name: Lunch
- Calories: 650
- Protein: 30g
- Carbs: 70g
- Fat: 22g
- Fiber: 6g
- Sugars: 8g

Dinner (18:30)
- Name: Dinner
- Calories: 700
- Protein: 32g
- Carbs: 80g
- Fat: 24g
- Fiber: 7g
- Sugars: 10g

Daily Total: 1550 kcal
```

**Total Daily Intake**: 1550 kcal
**Meal Distribution**: Snack + Lunch + Dinner (skipped breakfast)

**Patterns Triggered** (After Evaluate):
- ✓ **BreakfastSkippingPattern**: Breakfast skipped (requires 3+ days in CEP)
- Check calorie status (typically MAINTENANCE if goal = 1550-2000)

**Recommendations Generated**:
- "Breakfast is frequently skipped. Eating breakfast improves energy, focus, and metabolic health."
- If deficit: "Consider adding calories earlier in day."

**Use Case**:
- Demonstrate breakfast pattern detection
- Show impact of meal timing on daily intake
- Show recommendation for macro distribution

---

### 3. Eating Less Than Goal

**Button**: "Eating less than goal"

**What It Simulates**:
- Small breakfast (250 kcal)
- Light lunch (350 kcal)
- Light dinner (300 kcal)
- Total: 900 kcal (below typical 1800-2000 goal)

**Meal Data**:
```
Breakfast (08:00)
- Name: Small breakfast
- Calories: 250
- Protein: 10g
- Carbs: 30g
- Fat: 8g
- Fiber: 4g
- Sugars: 8g

Lunch (12:30)
- Name: Light lunch
- Calories: 350
- Protein: 15g
- Carbs: 42g
- Fat: 12g
- Fiber: 5g
- Sugars: 6g

Dinner (18:00)
- Name: Light dinner
- Calories: 300
- Protein: 12g
- Carbs: 35g
- Fat: 10g
- Fiber: 4g
- Sugars: 5g

Daily Total: 900 kcal
```

**Total Daily Intake**: 900 kcal (900 kcal below typical target)

**Patterns Triggered** (After Evaluate):
- ✓ **DailyIntakeStatus**: DEFICIT (< 70% of target)
- ✓ **ConsistentProteinDeficit**: Protein 37g vs target 105g (for 75kg female)
- ✓ **LongFastDetected**: Possibly (18+ hours without food)

**Recommendations Generated**:
- "Your intake is significantly below target. Consider adding nutritious calories for sustainable weight loss."
- "Protein target: 105g, achieved: 37g. Add protein-rich foods like eggs, chicken, or legumes."
- "Are you eating enough? Severe restriction is not sustainable."

**Use Case**:
- Demonstrate deficit detection
- Show protein macro tracking
- Show concern-level recommendations
- Useful for weight loss goals (but too extreme)

---

### 4. High Protein Day

**Button**: "High protein day"

**What It Simulates**:
- 5 meals throughout the day
- Each meal optimized for protein (20-45g per meal)
- Total: 200g+ protein (ideal for muscle gain goal)

**Meal Data**:
```
Breakfast (07:30) - Protein Pancakes
- Name: Protein pancakes
- Calories: 450
- Protein: 35g
- Carbs: 45g
- Fat: 12g
- Fiber: 4g
- Sugars: 8g

Snack (10:30) - Protein Shake
- Name: Protein shake
- Calories: 250
- Protein: 30g
- Carbs: 20g
- Fat: 5g
- Fiber: 1g
- Sugars: 8g

Lunch (12:45) - Grilled Chicken
- Name: Grilled chicken with rice
- Calories: 650
- Protein: 45g
- Carbs: 70g
- Fat: 15g
- Fiber: 5g
- Sugars: 2g

Snack (16:00) - Protein Bar
- Name: Protein bar
- Calories: 200
- Protein: 20g
- Carbs: 18g
- Fat: 6g
- Fiber: 3g
- Sugars: 5g

Dinner (19:00) - Salmon
- Name: Salmon with vegetables
- Calories: 550
- Protein: 40g
- Carbs: 35g
- Fat: 22g
- Fiber: 6g
- Sugars: 3g

Daily Total: 2100 kcal, 170g Protein
```

**Total Daily Intake**: 
- Calories: 2100 kcal
- Protein: **170g** (1.65g per kg body weight, ideal for muscle gain)
- Carbs: 188g
- Fat: 60g

**Patterns Triggered** (After Evaluate):
- ✓ **DailyIntakeStatus**: MAINTENANCE or SURPLUS (depending on goal)
- ✓ **No deficit patterns**: All macros well-covered
- ✓ **Possible recommendation**: "Excellent protein intake for muscle gain goal!"

**Recommendations Generated**:
- "Great job hitting protein targets! This supports muscle growth and recovery."
- "Distributed meals well throughout day—this supports muscle protein synthesis."
- Calorie status message (surplus/maintenance)

**Use Case**:
- Demonstrate positive scenario (user doing well)
- Show how system handles multiple meals
- Show macro distribution across meals
- Demonstrate recommendation for muscle gain goal
- Show good meal timing (5 meals spread throughout day)

---

## Using Test Scenarios in Defense

### Scenario 1: Quick Demo (5 minutes)
```
1. Go to Dashboard tab
2. Click "Late night binge" button
3. Meals appear instantly
4. Click "Evaluate" button
5. Show recommendations appear
6. Point to "LateNightEatingPattern" in chips
7. Explain: "CEP detected 2 meals in 20-minute window"
```

### Scenario 2: Pattern Demonstration (3 minutes)
```
1. Click "Seed Expert Scenario" (not test button)
2. Wait ~2 seconds
3. Multiple days populated with breakfast skip + late dinner
4. Click "Evaluate"
5. Say: "Now we see the pattern across 3 days detected"
6. Show BreakfastSkippingPattern + LateNightEatingPattern both appear
```

### Scenario 3: Healthy Scenario (2 minutes)
```
1. Click "High protein day"
2. Meals appear (5 meals)
3. Click "Evaluate"
4. Show no warnings
5. Show protein macro: 170g achieved (100% of goal+)
6. Say: "System recognizes good behavior and affirms it"
```

### Scenario 4: Deficit Case (2 minutes)
```
1. Click "Eating less than goal"
2. Meals appear (small portions)
3. Click "Evaluate"
4. Show red/warning recommendations
5. Show deficit status
6. Explain: "System detects under-eating and suggests adding calories"
```

---

## How Test Scenarios Work (Technical)

The scenarios are buttons that call `loadTestScenario(scenario: string)` in `app.component.ts`:

```typescript
loadTestScenario(scenario: string): void {
  const journal = this.selectedJournal;
  journal.meals = [];  // Clear existing
  
  switch(scenario) {
    case 'lateNightBinge':
      journal.skippedMeals.push({
        userId: this.state.user.id,
        category: 'BREAKFAST',
        date: selectedDate
      });
      journal.meals.push({
        name: 'Large late dinner',
        category: 'DINNER',
        calories: 1200,
        protein: 35,
        timestamp: `${selectedDate}T23:30:00`,
        ...
      });
      // Add more meals...
      break;
    
    case 'breakfastSkipping':
      // Similar logic
      break;
    
    // ... other scenarios
  }
  
  this.persistState();
  this.evaluateSelectedDay();
}
```

**Key Points**:
- Clears previous meals
- Populates specific meals for the scenario
- Calls `persistState()` to save to localStorage
- Calls `evaluateSelectedDay()` to run backend evaluation

---

## Extending Test Scenarios

**To Add a New Scenario**:

1. **Frontend** - Add button in HTML:
```html
<button type="button" class="scenario-btn" (click)="loadTestScenario('newScenario')">
  My New Scenario
</button>
```

2. **Frontend** - Add case in TypeScript:
```typescript
case 'newScenario':
  journal.meals.push({
    name: 'Meal 1',
    category: 'BREAKFAST',
    calories: 300,
    protein: 20,
    timestamp: `${selectedDate}T08:00:00`,
    userId: this.state.user.id
  });
  // Add more meals for scenario
  break;
```

3. **Backend** - No changes needed (already handles all meal types)

---

## Data Persistence

All test scenarios are stored in **browser localStorage** under key:
```
adaptive-nutrition-dashboard-state-v1
```

**What This Means**:
- Scenarios persist across page reloads
- Data is local to this browser (not sent to server initially)
- Only sent to backend when "Evaluate" is clicked
- Clear browser cache to reset

**Clearing Data**:
- Click "Clear selected day" button (removes single day)
- Or clear browser localStorage:
  ```javascript
  localStorage.removeItem('adaptive-nutrition-dashboard-state-v1')
  ```

---

## Test Scenario Checklist (Before Defense)

For each scenario, verify:

### Late Night Binge
- [ ] Button exists on page
- [ ] Clicking adds 2 meals (dinner + snack)
- [ ] Breakfast is marked as skipped
- [ ] Times are 23:30 and 23:50
- [ ] Total calories ≈ 1600
- [ ] Evaluate shows recommendations

### Breakfast Skipping
- [ ] Button exists on page
- [ ] Clicking adds 3 meals (no breakfast)
- [ ] Snack time is 10:30
- [ ] Total calories ≈ 1550
- [ ] Evaluate shows deficit recommendation

### Eating Less Than Goal
- [ ] Button exists on page
- [ ] Clicking adds 3 small meals
- [ ] Total calories ≈ 900
- [ ] Evaluate shows DEFICIT status
- [ ] Protein deficit message appears

### High Protein Day
- [ ] Button exists on page
- [ ] Clicking adds 5 meals
- [ ] Total calories ≈ 2100
- [ ] Protein total ≈ 170g
- [ ] Evaluate shows maintenance/surplus status
- [ ] Positive recommendation appears

---

## Demonstration Talking Points

**For Evaluators**:
"These test scenarios let me quickly demonstrate different nutrition situations without spending 10 minutes manually entering meal data. Each button represents a realistic eating pattern that our system should recognize and provide guidance on."

**For Scenarios**:

1. **Late Night Binge**: 
   - "Shows CEP detecting meals in specific time window (22:00-06:00)"
   - "Multiple patterns triggered from single scenario"

2. **Breakfast Skipping**:
   - "For CEP demo, I'd use 'Seed Expert Scenario' which repeats this pattern over 3 days"
   - "System detects the behavioral pattern emerging"

3. **Eating Less**:
   - "Shows system handles extreme cases (deficit detection)"
   - "Demonstrates concern-level recommendations"

4. **High Protein**:
   - "Shows system recognizing good behavior"
   - "Demonstrates macro-specific guidance"

---

## FAQ - Test Data

**Q: Why auto-populate meals instead of letting me enter them?**
A: "Time. Entering 10+ fields per meal × 5 meals = 50 form entries. The test buttons compress this to single click, letting me focus on explaining the intelligence, not data entry."

**Q: Can I modify the scenario meals after loading?**
A: "Yes. Click the button to populate, then manually edit any field before evaluating. The form is still editable."

**Q: What if I want a different scenario?**
A: "You can load any scenario and manually add/remove meals before evaluating. The buttons are just starting points."

**Q: Does the backend know about these test scenarios?**
A: "No. The backend sees them as normal meal data. The scenarios are purely frontend convenience for testing."

**Q: Can students modify scenarios for their own project?**
A: "Absolutely. The code is open. Add your own scenarios by adding buttons + cases in the component."

