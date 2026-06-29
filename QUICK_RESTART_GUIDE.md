# Quick Restart Guide - After Fixes

## 🔄 Steps to Get Running (5 minutes)

### Step 1: Stop Current Services
- Kill the running Spring Boot backend (Ctrl+C)
- Kill the running Angular frontend (Ctrl+C)
- Clear browser cache (Ctrl+Shift+Delete)

### Step 2: Rebuild Backend
```bash
cd service
mvn clean install
mvn spring-boot:run
```

Wait for: `Tomcat started on port 8080 with context path ''`

### Step 3: Start Frontend
```bash
cd frontend
npm install  # Only if first time
ng serve
```

Wait for: `ng serve | Application bundle generation complete`

### Step 4: Open Browser
```
http://localhost:4200
```

---

## 🧪 Quick Test (2 minutes)

### Test 1: Check Backend Sync
1. Go to **Goals** tab
2. Fill in profile (Female, 30, 75kg, 165cm, weight loss goal)
3. Save
4. Go to **Dashboard** tab
5. Click **"Small Breakfast"** button
6. Click **"Evaluate"** button
7. ✅ Check that "Expert Opinion" card shows:
   - `Expert summary` text
   - `Calorie Status:` explanation
   - `Protein:` explanation  
   - `Analysis details` section

### Test 2: Check Meal Appending
1. Click **"Small Breakfast"** again
2. Click **"Evaluate"**
3. ✅ Check "Logged meals" card shows **2 meals** (not 1)
4. ✅ Total calories = 250 + 250 = 500

### Test 3: Check Test Scenarios
1. Click **"Late night binge"** button
2. ✅ Check 2 meals appear (dinner + snack)
3. Click **"Small Lunch"** button
4. ✅ Check "Logged meals" shows **3 meals** (test scenario + lunch)

---

## 📊 What Should Display

### Expert Opinion Card Shows:
```
[Status] - e.g., "DEFICIT"

[Progress bar] - showing % of daily goal

[Stats]
- Consumed: XXX kcal
- Target: XXX kcal
- Remaining: XXX kcal

[Expert Summary]
"Calorie deficit detected. Your intake is below your daily target..."

[Calorie Status]
"Undereating: only 54% of target (838 kcal remaining)..."

[Protein]
"Protein deficit: 52% of target (50g remaining). Add lean meats..."

[Fiber]
"Low fiber intake: 72% of target (7g remaining)..."

[Analysis Details]
- BMR: 1500 kcal
- Target: 1825 kcal
- Meals logged: 3
- Total calories: 1000 kcal (54% of target)
- Rules fired: 8
- Patterns: BreakfastSkippingPattern
```

---

## 🍽️ Quick Meals Reference

### Breakfast
| Button | Cals | Protein | Time |
|--------|------|---------|------|
| Small | 250 | 8g | 08:00 |
| Medium | 400 | 15g | 08:00 |
| Large | 600 | 25g | 08:00 |

### Lunch
| Button | Cals | Protein | Time |
|--------|------|---------|------|
| Small | 400 | 20g | 12:30 |
| Medium | 650 | 35g | 12:30 |
| Large | 900 | 50g | 12:30 |

### Dinner
| Button | Cals | Protein | Time |
|--------|------|---------|------|
| Small | 400 | 25g | 18:30 |
| Medium | 700 | 40g | 18:30 |
| Large | 950 | 55g | 18:30 |

### Snacks
| Button | Cals | Protein | Time |
|--------|------|---------|------|
| Healthy | 150 | 8g | 15:00 |
| Bad/Junk | 250 | 3g | 15:00 |
| Past Midnight | 400 | 10g | 23:30 |

---

## 🐛 Troubleshooting

### Issue: "Backend not responding"
```
DELETE: Browser cache
REBUILD: Backend (mvn clean install)
RESTART: Both services
```

### Issue: "Expert Opinion card empty"
1. Check DevTools → Network → Response payload
2. Look for `expertOpinionSummary` field
3. If missing: Backend not sending it
4. Solution: Restart backend

### Issue: "Quick meals button not working"
1. Check that you're on Dashboard tab
2. Check that user profile exists (go to Goals first)
3. Try clicking "Evaluate" after clicking meal

### Issue: "Meals disappearing"
1. This should NOT happen anymore
2. Test scenarios should APPEND to existing meals
3. If it does: Check browser console for errors

### Issue: "Percentages showing 0%"
1. Go to Goals tab
2. Make sure all fields filled (esp. height)
3. Click "Save goals"
4. Return to Dashboard and try again

---

## 📝 Files That Changed

### Backend (Java)
- ✅ `NutritionEvaluationResponse.java` - 10 new fields
- ✅ `NutritionEvaluationController.java` - 4 new helper methods

### Frontend (TypeScript/HTML/CSS)
- ✅ `nutrition-api.service.ts` - Updated interface
- ✅ `app.component.ts` - New QuickMeal interface + addQuickMeal() method
- ✅ `app.component.html` - Enhanced expert card + quick meals section
- ✅ `app.component.css` - New styling for explanations

---

## ✅ Success Checklist

Before going to defense, verify:

- [ ] Backend starts without errors
- [ ] Frontend loads on localhost:4200
- [ ] Can create user profile and save
- [ ] Quick meal buttons work
- [ ] Meals append (not replace)
- [ ] Test scenarios work
- [ ] Expert opinion card shows all sections
- [ ] Reasoning trace displays
- [ ] Status reasons explain why
- [ ] Network response has new fields
- [ ] No console errors
- [ ] All meals show in logged list

---

## 🎯 For Defense Demo

**Recommended flow**:

1. **Setup** (1 min)
   - Go to Goals, create profile
   - Save and note the calculated targets

2. **Quick Demo** (2 min)
   - Click "Small Breakfast" + "Medium Lunch" + "Small Dinner"
   - Click "Evaluate"
   - Point to expert opinion showing undereating

3. **Pattern Demo** (3 min)
   - Click "Late night binge"
   - Meals populate automatically
   - Click "Evaluate"
   - Show pattern detected in expert card

4. **Explanation** (2 min)
   - Explain each field in expert card
   - Show reasoning trace
   - Show how backend returns detailed analysis

---

## 📞 Quick Fixes During Demo

If something breaks:

**Expert card not showing**:
- Refresh page (F5)
- Check Network tab to verify response has data

**Quick meals not working**:
- Verify you're on Dashboard (not Goals)
- Try clicking "Evaluate" after clicking meal

**Percentages wrong**:
- Go to Goals, verify all values filled correctly
- Recalculate will happen on first Evaluate

**Meals disappeared**:
- Should not happen - new code prevents this
- If it does: Refresh and try again

---

## 🎓 What Makes This System "Expert"

For your defense, emphasize:

1. **Not just totaling calories** - System analyzes patterns
2. **Personalized guidance** - Recommendations fit user's goal
3. **Explainable AI** - User sees WHY system recommends something
4. **Smart meal selection** - Quick meals help testing without manual entry
5. **Real-time feedback** - Response explains every calculation

---

## Questions You Might Get

**Q: Why the quick meals?**
A: Testing without spending 10 minutes entering food data manually. Makes demo faster, shows how system handles different scenarios.

**Q: Why append instead of replace?**
A: Users should be able to add to test scenarios, not lose them. Reflects real usage where people add snacks throughout day.

**Q: Why so much explanation?**
A: Expert systems should be transparent. Users need to understand WHY they're getting a recommendation, not just what it is.

**Q: Why percentages?**
A: More intuitive than raw numbers. "65% of target" means more to people than "1200/1825 kcal".

---

**Everything is ready. You're good to demo!** 🚀

