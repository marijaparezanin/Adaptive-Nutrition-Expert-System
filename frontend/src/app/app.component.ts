import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  DailyIntake,
  GoalType,
  MealCategory,
  MealEntry,
  MissedMealAlert,
  NewActivityRoutine,
  NutritionApiService,
  NutritionEvaluationRequest,
  NutritionEvaluationResponse,
  SkippedMeal,
  UserProfile,
  WeeklyPattern,
  WeightMeasured,
  WeightTrend
} from './nutrition-api.service';

interface DayJournal {
  date: string;
  meals: MealEntry[];
  skippedMeals: SkippedMeal[];
}

interface AppState {
  user: UserProfile;
  journals: Record<string, DayJournal>;
  weightMeasurements: WeightMeasured[];
  evaluations: Record<string, NutritionEvaluationResponse>;
}

interface MealForm {
  name: string;
  category: MealCategory;
  time: string;
  calories: number;
  protein: number;
  carbohydrates: number;
  fat: number;
  fiber: number;
  sugars: number;
}

interface QuickMeal {
  id: string;
  label: string;
  category: MealCategory;
  calories: number;
  protein: number;
  carbohydrates: number;
  fat: number;
  fiber: number;
  sugars: number;
}

export interface AnalyticSignal {
  type: string;
  severity: string;
  label: string;
  severityClass: string;
}

export interface ScoredRecommendation {
  type: string;
  message: string;
  severityClass: string;
}

const STORAGE_KEY = 'adaptive-nutrition-dashboard-state-v2';

const STATUS_LABELS: Record<string, string> = {
  PROTEIN_WARNING: 'Protein Warning',
  PROTEIN_DISTRIBUTION_CONFLICT: 'Protein Distribution Conflict',
  PROTEIN_CRITICAL: 'Protein Critical',
  ENERGY_SHORTAGE: 'Energy Shortage',
  OVEREATING_DETECTED: 'Overeating Detected',
  MUSCLE_LOSS_RISK: 'Muscle Loss Risk',
  WEIGHT_STAGNATION_CONFIRMED: 'Weight Stagnation',
  CALORIC_ADAPTATION_BLOCK: 'Metabolic Adaptation',
  PLAN_CHANGE_NEEDED: 'Plan Change Needed',
  EXTREME_CALORIC_RESTRICTION: 'Extreme Restriction',
  CALORIC_SURPLUS_ACCUMULATION: 'Caloric Surplus',
  LOW_PROTEIN_MEAL: 'Low Protein Meal',
  PROTEIN_INSUFFICIENT: 'Protein Insufficient',
  CIRCADIAN_RHYTHM_DISRUPTION: 'Circadian Disruption',
  CALORIE_SPIKE_DETECTED: 'Calorie Spike',
  LONG_FAST_DETECTED: 'Long Fast',
  HIGH_PROTEIN_HYDRATION_NEEDED: 'Hydration Alert',
  WEIGHT_STAGNATION_DETECTED: 'Weight Stagnation Detected',
  MISSED_MEAL_DETECTED: 'Missed Meal',
  SURPLUS_WITH_DEFICIT_GOAL: 'Surplus vs Goal',
  SNACK_OVERLOAD_CALORIC_RISK: 'Snack Overload',
  FIBER_CRITICALLY_LOW: 'Fiber Critical',
  LINKED_PATTERN_STAGNATION_RISK: 'Linked Pattern - Stagnation Risk',
  WEEKLY_PROTEIN_DEFICIT: 'Weekly Protein Deficit',
  REACTIVE_EATING_CONFIRMED: 'Reactive Eating Cycle Confirmed',
  GOAL_CONFLICT_ACTIVITY_NEEDED: 'Goal Conflict - Activity Needed'
};

const RECOMMENDATION_SEVERITY: Record<string, string> = {
  PLAN_CHANGE_NEEDED: 'critical',
  PROTEIN_CRITICAL: 'critical',
  MASS_GAIN_CONFLICT: 'critical',
  COMPOUND_NUTRITION_FAILURE: 'critical',
  WEIGHT_LOSS_CONFLICT: 'high',
  BINGE_WARNING: 'high',
  OVEREATING: 'high',
  MUSCLE_LOSS_RISK: 'high',
  SURPLUS_SPIKE_COMBO: 'high',
  SNACK_OVERLOAD: 'high',
  FAST_PLUS_MISSED_MEAL: 'high',
  QUALITY_DEFICIT: 'high',
  BREAKFAST_SKIPPING: 'warning',
  CALORIE_SPIKE: 'warning',
  LATE_NIGHT_EATING_PATTERN: 'warning',
  LATE_NIGHT_SPIKE: 'warning',
  LONG_FAST: 'warning',
  LOW_PROTEIN_MEAL: 'warning',
  BINGE_PATTERN: 'warning',
  MISSED_BREAKFAST: 'warning',
  MISSED_LUNCH: 'warning',
  PROTEIN_DISTRIBUTION: 'warning',
  FIBER_LOW: 'warning',
  CIRCADIAN_FAST_COMBO: 'warning',
  LATE_NIGHT_WEIGHT_LOSS_CONFLICT: 'high',
  BINGE_SURPLUS: 'high',
  LINKED_PATTERN_STAGNATION: 'critical',
  REACTIVE_EATING_SYNTHESIS: 'critical',
  COMPOUND_REACTIVE_PROTEIN: 'critical',
  WEEKLY_PROTEIN_DEFICIT: 'high',
  GOAL_CONFLICT_ACTIVITY: 'high',
  FAST_CIRCADIAN_COMBO: 'warning',
  BREAKFAST_SKIPPED_TODAY: 'warning',
  MULTIPLE_SKIPS: 'warning',
  CHRONIC_LATE_EATING: 'high',
  HIGH_PROTEIN_HYDRATION: 'info',
  ACTIVITY_INCREASE: 'info',
  WEIGHT_OSCILLATION: 'info'
};

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  readonly categories: MealCategory[] = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'];
  readonly goals: GoalType[] = ['WEIGHT_LOSS', 'MAINTENANCE', 'MUSCLE_GAIN'];
  readonly activities = [
    { label: 'Sedentary', value: 1.2 },
    { label: 'Light', value: 1.375 },
    { label: 'Moderate', value: 1.55 },
    { label: 'Very active', value: 1.725 }
  ];

  readonly quickMeals: QuickMeal[] = [
    { id: 'bf-small', label: 'Small Breakfast', category: 'BREAKFAST', calories: 250, protein: 8, carbohydrates: 35, fat: 8, fiber: 3, sugars: 10 },
    { id: 'bf-medium', label: 'Medium Breakfast', category: 'BREAKFAST', calories: 400, protein: 15, carbohydrates: 45, fat: 12, fiber: 5, sugars: 12 },
    { id: 'bf-large', label: 'Large Breakfast', category: 'BREAKFAST', calories: 600, protein: 25, carbohydrates: 65, fat: 18, fiber: 8, sugars: 15 },
    { id: 'ln-small', label: 'Small Lunch', category: 'LUNCH', calories: 400, protein: 20, carbohydrates: 45, fat: 12, fiber: 6, sugars: 5 },
    { id: 'ln-medium', label: 'Medium Lunch', category: 'LUNCH', calories: 650, protein: 35, carbohydrates: 70, fat: 20, fiber: 8, sugars: 8 },
    { id: 'ln-large', label: 'Large Lunch', category: 'LUNCH', calories: 900, protein: 50, carbohydrates: 95, fat: 28, fiber: 10, sugars: 10 },
    { id: 'dn-small', label: 'Small Dinner', category: 'DINNER', calories: 400, protein: 25, carbohydrates: 40, fat: 12, fiber: 5, sugars: 4 },
    { id: 'dn-medium', label: 'Medium Dinner', category: 'DINNER', calories: 700, protein: 40, carbohydrates: 70, fat: 22, fiber: 8, sugars: 6 },
    { id: 'dn-large', label: 'Large Dinner', category: 'DINNER', calories: 950, protein: 55, carbohydrates: 95, fat: 30, fiber: 10, sugars: 8 },
    { id: 'sn-healthy', label: 'Healthy Snack', category: 'SNACK', calories: 150, protein: 8, carbohydrates: 18, fat: 4, fiber: 3, sugars: 10 },
    { id: 'sn-bad', label: 'Junk Snack', category: 'SNACK', calories: 250, protein: 3, carbohydrates: 35, fat: 12, fiber: 1, sugars: 25 },
    { id: 'sn-midnight', label: 'Late Night Meal', category: 'SNACK', calories: 400, protein: 10, carbohydrates: 50, fat: 14, fiber: 2, sugars: 30 }
  ];

  activeView = signal<'dashboard' | 'goals'>('dashboard');
  selectedDate = signal(this.toDateKey(new Date()));
  weekStart = signal(this.getWeekStart(new Date()));
  evaluation = signal<NutritionEvaluationResponse | null>(null);
  apiError = signal<string | null>(null);
  loading = signal(false);
  stagedMeal = signal<QuickMeal | null>(null);
  // Bump this signal whenever journals/evaluations mutate so computed() re-runs
  journalVersion = signal(0);

  state: AppState = this.defaultState();
  mealForm: MealForm = this.emptyMealForm();
  newWeight = this.state.user.weight;
  newWeightDate = this.toDateKey(new Date());
  newWeightTime = '08:00';

  weekDays = computed(() => {
    // Depend on journalVersion so this re-runs when meals are added/removed
    this.journalVersion();
    const start = this.weekStart();
    return Array.from({ length: 7 }, (_, index) => {
      const date = new Date(start);
      date.setDate(start.getDate() + index);
      const key = this.toDateKey(date);
      const journal = this.getJournal(key);
      const calories = journal.meals.reduce((sum, meal) => sum + Number(meal.calories || 0), 0);
      const ev = this.state.evaluations[key];
      const maxSeverity = this.getMaxSeverity(ev?.analyticStatuses ?? [], ev?.analyticSeverities ?? []);
      return {
        key,
        label: date.toLocaleDateString(undefined, { weekday: 'short' }),
        day: date.getDate(),
        calories,
        meals: journal.meals.length,
        warnings: ev?.analyticStatuses.length || 0,
        maxSeverity
      };
    });
  });

  constructor(private readonly nutritionApi: NutritionApiService) {}

  ngOnInit(): void {
    this.restoreState();
    this.newWeight = this.state.user.weight;
    this.evaluateSelectedDay();
  }

  get selectedJournal(): DayJournal {
    return this.getJournal(this.selectedDate());
  }

  get selectedMeals(): MealEntry[] {
    return this.selectedJournal.meals;
  }

  get selectedDateForDisplay(): string {
    // Safely parse date string to avoid timezone offset issues
    const [year, month, day] = this.selectedDate().split('-').map(Number);
    const d = new Date(year, month - 1, day);
    return d.toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
  }

  get weeklySummary(): { calories: number; protein: number; fiber: number; meals: number; warnings: number; recommendations: number; criticalCount: number } {
    const journals = this.weekDays().map((day) => this.getJournal(day.key));
    const evaluations = this.weekDays().map((day) => this.state.evaluations[day.key]).filter(Boolean);
    const meals = journals.flatMap((j) => j.meals);
    return {
      calories: meals.reduce((s, m) => s + Number(m.calories || 0), 0),
      protein: meals.reduce((s, m) => s + Number(m.protein || 0), 0),
      fiber: meals.reduce((s, m) => s + Number(m.fiber || 0), 0),
      meals: meals.length,
      warnings: evaluations.reduce((s, r) => s + r.analyticStatuses.length, 0),
      recommendations: evaluations.reduce((s, r) => s + r.recommendations.length, 0),
      criticalCount: evaluations.reduce((s, r) => s + (r.analyticSeverities?.filter(sv => sv === 'CRITICAL').length || 0), 0)
    };
  }

  /** Analytic signals enriched with labels and severity class */
  get analyticSignals(): AnalyticSignal[] {
    const ev = this.evaluation();
    if (!ev) return [];
    return (ev.analyticStatuses || []).map((type, i) => {
      const severity = (ev.analyticSeverities || [])[i] || 'WARNING';
      return {
        type,
        severity,
        label: STATUS_LABELS[type] || type.replace(/_/g, ' '),
        severityClass: 'sev-' + severity.toLowerCase()
      };
    });
  }

  /** Recommendations enriched with severity class */
  get scoredRecommendations(): ScoredRecommendation[] {
    const ev = this.evaluation();
    if (!ev) return [];
    return (ev.recommendations || []).map((message, i) => {
      const type = (ev.recommendationTypes || [])[i] || '';
      const severityKey = RECOMMENDATION_SEVERITY[type] || 'warning';
      return { type, message, severityClass: 'rec-' + severityKey };
    });
  }

  get expertOpinionLevel(): string {
    const ev = this.evaluation();
    if (!ev) return 'ok';
    // Severity from backend analytic statuses takes priority
    if (ev.analyticSeverities?.includes('CRITICAL')) return 'critical';
    if (ev.analyticSeverities?.includes('HIGH')) return 'high';
    if (ev.analyticSeverities?.includes('WARNING')) return 'warning';
    // Goal conflict: surplus when goal is weight loss, or deficit when goal is muscle gain
    // This fires even when no AnalyticStatus was generated (e.g. intake just crossed 110% threshold)
    const isGoalConflict =
      (this.state.user.goal === 'WEIGHT_LOSS' && ev.dailyStatus === 'SURPLUS') ||
      (this.state.user.goal === 'MUSCLE_GAIN' && ev.dailyStatus === 'DEFICIT');
    if (isGoalConflict) return 'high';
    // Any recommendations from backend count as at least a warning
    if ((ev.recommendations?.length || 0) > 0) return 'warning';
    return 'ok';
  }

  get expertStatusLabel(): string {
    const ev = this.evaluation();
    if (!ev) return 'Awaiting input';
    if (ev.analyticSeverities?.includes('CRITICAL')) return 'Critical alert';
    if (ev.analyticSeverities?.includes('HIGH')) return 'Attention needed';
    if (ev.analyticStatuses?.length) return 'Signals detected';
    // Goal conflict: surplus when trying to lose weight, or deficit when trying to gain
    if (this.state.user.goal === 'WEIGHT_LOSS' && ev.dailyStatus === 'SURPLUS') return 'Goal conflict - surplus';
    if (this.state.user.goal === 'MUSCLE_GAIN' && ev.dailyStatus === 'DEFICIT') return 'Goal conflict - deficit';
    if (ev.dailyStatus === 'SURPLUS') return 'Caloric surplus';
    if (ev.dailyStatus === 'DEFICIT') return 'Caloric deficit';
    if (ev.dailyStatus === 'MAINTENANCE_STABLE') return 'On target';
    return 'All clear';
  }

  get calorieProgress(): number {
    const response = this.evaluation();
    if (!response || response.targetCalories <= 0) return 0;
    return Math.min(100, Math.max(0, (response.totalCalories / response.targetCalories) * 100));
  }

  get calorieProgressClass(): string {
    const pct = this.calorieProgress;
    if (pct > 110) return 'progress-over';
    if (pct >= 80) return 'progress-good';
    if (pct >= 50) return 'progress-low';
    return 'progress-critical';
  }

  get isSurplus(): boolean {
    return this.evaluation()?.dailyStatus === 'SURPLUS';
  }

  get calorieAmountClass(): string {
    const ev = this.evaluation();
    if (!ev) return '';
    if (ev.dailyStatus === 'SURPLUS') return 'calorie-surplus';
    if ((ev.caloriePercentage || 0) < 50) return 'calorie-critical';
    return '';
  }

  /** Filter out idle boilerplate text - only show if no real signals exist */
  get expertOpinionText(): string {
    const ev = this.evaluation();
    if (!ev) return '';
    const summary = ev.expertOpinionSummary || '';
    // If there are patterns or analytic statuses, strip the generic idle sentence
    const hasSignals = (ev.analyticStatuses?.length || 0) > 0 || (ev.detectedPatterns?.length || 0) > 0;
    if (hasSignals) {
      // Remove generic idle text, keep the rest (daily balance, CEP pattern count)
      return summary
        .replace('No active expert signals. Your nutrition data for this day appears balanced. Continue logging consistently to enable multi-day pattern detection.', '')
        .trim()
        .replace(/^\|\s*/, '');
    }
    return summary;
  }

  /** Filter out idle recommendation boilerplate */
  get hasRealRecommendations(): boolean {
    return (this.evaluation()?.recommendations?.length || 0) > 0;
  }

  /** True if a meal with category BREAKFAST is logged today */
  get hasBreakfastMeal(): boolean {
    return this.selectedMeals.some(m => m.category === 'BREAKFAST');
  }

  /** True when backend CEP has already fired ENERGY_SHORTAGE (breakfast skip already signalled) */
  get hasEnergyShortageSignal(): boolean {
    return (this.evaluation()?.analyticStatuses || []).includes('ENERGY_SHORTAGE');
  }

  get tdeePreview(): { bmr: number; targetCalories: number; targetProtein: number; targetFiber: number } {
    const bmr = this.calculateBmr(this.state.user);
    const adjustment = this.state.user.goal === 'WEIGHT_LOSS' ? -500 : this.state.user.goal === 'MUSCLE_GAIN' ? 300 : 0;
    const proteinMultiplier = this.state.user.goal === 'MUSCLE_GAIN' ? 1.8 : this.state.user.goal === 'WEIGHT_LOSS' ? 1.4 : 1;
    return {
      bmr,
      targetCalories: bmr * this.state.user.activityFactor + adjustment,
      targetProtein: this.state.user.weight * proteinMultiplier,
      targetFiber: this.state.user.gender === 'FEMALE' ? 25 : 38
    };
  }

  changeWeek(offset: number): void {
    const next = new Date(this.weekStart());
    next.setDate(next.getDate() + offset * 7);
    this.weekStart.set(next);
    this.selectedDate.set(this.toDateKey(next));
    this.evaluateSelectedDay();
  }

  selectDay(date: string): void {
    this.selectedDate.set(date);
    this.evaluateSelectedDay();
  }

  addMeal(): void {
    if (!this.mealForm.name.trim() && !this.mealForm.calories) return;
    const meal: MealEntry = {
      name: this.mealForm.name.trim() || this.formatCategory(this.mealForm.category),
      category: this.mealForm.category,
      calories: Number(this.mealForm.calories || 0),
      protein: Number(this.mealForm.protein || 0),
      carbohydrates: Number(this.mealForm.carbohydrates || 0),
      fat: Number(this.mealForm.fat || 0),
      fiber: Number(this.mealForm.fiber || 0),
      sugars: Number(this.mealForm.sugars || 0),
      timestamp: `${this.selectedDate()}T${this.mealForm.time}:00`,
      userId: this.state.user.id,
      processed: false
    };
    this.selectedJournal.meals.push(meal);
    this.journalVersion.update(v => v + 1);
    this.persistState();
    this.mealForm = this.emptyMealForm();
    this.evaluateSelectedDay();
  }

  selectQuickMeal(quickMeal: QuickMeal): void {
    this.stagedMeal.set(quickMeal);
  }

  clearStagedMeal(): void {
    this.stagedMeal.set(null);
  }

  confirmStagedMeal(): void {
    const quick = this.stagedMeal();
    if (!quick) return;
    let time = '15:00';
    if (quick.category === 'BREAKFAST') time = '08:00';
    else if (quick.category === 'LUNCH') time = '12:30';
    else if (quick.category === 'DINNER') time = '18:30';
    else if (quick.id === 'sn-midnight') time = '23:30';

    const meal: MealEntry = {
      name: quick.label,
      category: quick.category,
      calories: quick.calories,
      protein: quick.protein,
      carbohydrates: quick.carbohydrates,
      fat: quick.fat,
      fiber: quick.fiber,
      sugars: quick.sugars,
      timestamp: `${this.selectedDate()}T${time}:00`,
      userId: this.state.user.id,
      processed: false
    };
    this.selectedJournal.meals.push(meal);
    this.journalVersion.update(v => v + 1);
    this.persistState();
    this.stagedMeal.set(null);
    this.evaluateSelectedDay();
  }

  removeMeal(index: number): void {
    this.selectedJournal.meals.splice(index, 1);
    this.journalVersion.update(v => v + 1);
    this.persistState();
    this.evaluateSelectedDay();
  }

  toggleSkippedBreakfast(checked: boolean): void {
    const journal = this.selectedJournal;
    journal.skippedMeals = checked
      ? [{ userId: this.state.user.id, category: 'BREAKFAST', date: this.selectedDate() }]
      : [];
    this.persistState();
    this.evaluateSelectedDay();
  }

  isBreakfastSkipped(): boolean {
    return this.selectedJournal.skippedMeals.some((m) => m.category === 'BREAKFAST');
  }

  saveGoals(): void {
    this.persistState();
    this.evaluateSelectedDay();
    this.activeView.set('dashboard');
  }

  addWeightMeasurement(): void {
    const date = this.newWeightDate || this.selectedDate();
    const time = this.newWeightTime || '08:00';
    this.state.weightMeasurements.push({
      userId: this.state.user.id,
      weight: Number(this.newWeight || this.state.user.weight),
      timestamp: `${date}T${time}:00`
    });
    this.newWeightDate = this.toDateKey(new Date());
    this.persistState();
    this.journalVersion.update(v => v + 1);
    this.evaluateSelectedDay();
  }

  evaluateSelectedDay(): void {
    this.loading.set(true);
    this.apiError.set(null);
    this.journalVersion.update(v => v + 1);
    this.nutritionApi.evaluate(this.buildRequest()).subscribe({
      next: (response) => {
        this.evaluation.set(response);
        this.state.evaluations[this.selectedDate()] = response;
        this.persistState();
        this.journalVersion.update(v => v + 1);
        this.loading.set(false);
      },
      error: () => {
        this.apiError.set('Backend not reachable at http://localhost:8080. Start the Spring Boot service to enable expert analysis.');
        this.loading.set(false);
      }
    });
  }

  formatCategory(category: string): string {
    return category.charAt(0) + category.slice(1).toLowerCase().replace(/_/g, ' ');
  }

  severityIcon(severity: string): string {
    switch (severity) {
      case 'CRITICAL': return '🔴';
      case 'HIGH': return '🟠';
      case 'WARNING': return '🟡';
      case 'LOW': return 'ℹ';
      default: return '·';
    }
  }

  recommendationIcon(type: string): string {
    const sev = RECOMMENDATION_SEVERITY[type] || 'warning';
    switch (sev) {
      case 'critical': return '⛔';
      case 'high': return '⚠';
      case 'warning': return '!';
      case 'info': return 'ℹ';
      default: return '!';
    }
  }

  getMaxSeverity(statuses: string[], severities: string[]): string {
    if (severities.includes('CRITICAL')) return 'critical';
    if (severities.includes('HIGH')) return 'high';
    if (severities.includes('WARNING')) return 'warning';
    if (severities.includes('LOW')) return 'low';
    return '';
  }

  loadTestScenario(scenario: string): void {
    const selectedDate = this.selectedDate();
    const journal = this.selectedJournal;
    journal.meals = [];
    journal.skippedMeals = [];

    switch (scenario) {

      // ── S1: Goal Conflict - Surplus while goal is WEIGHT_LOSS
      // Fires: Rule_Weight_Loss_Conflict (surplus >10% + WEIGHT_LOSS goal)
      //        CEP Calorie Spike (lunch 1050 kcal > 50% of ~1800 target)
      //        Rule Surplus With Spike Combo → SURPLUS_WITH_DEFICIT_GOAL
      case 'surplusVsGoal':
        journal.meals.push(
          { name: 'Large breakfast', category: 'BREAKFAST', calories: 780, protein: 18, carbohydrates: 95, fat: 28, fiber: 4, sugars: 20, timestamp: `${selectedDate}T08:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Fast food lunch', category: 'LUNCH', calories: 1050, protein: 35, carbohydrates: 110, fat: 48, fiber: 5, sugars: 18, timestamp: `${selectedDate}T13:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Pizza dinner', category: 'DINNER', calories: 920, protein: 28, carbohydrates: 105, fat: 38, fiber: 6, sugars: 12, timestamp: `${selectedDate}T19:30:00`, userId: this.state.user.id, processed: false }
        );
        break;

      // ── S2: Pattern Linkage - Skip breakfast → ENERGY_SHORTAGE → compensatory binge → OVEREATING chain
      // Fires: CEP Today Breakfast Skipped (1 MealSkipped) → ENERGY_SHORTAGE
      //        Rule Pattern Linkage (ENERGY_SHORTAGE + meal >40% target) → OVEREATING_DETECTED
      //        CEP Late Night Spike (dinner at 22:30, 820 kcal > 30% of target) → CIRCADIAN_RHYTHM_DISRUPTION
      //        CEP Long Fast (gap 11:30 → 14:00 is fine, but no meal 07-11 = >4h gap skipped entirely)
      //        Rule Calorie Surplus Direct (total ~2630 kcal > 110% of ~1800 target)
      case 'breakfastSkipCompensation':
        // No BREAKFAST → derivedSkippedMeals generates 1 MealSkipped(BREAKFAST) for today
        journal.meals.push(
          { name: 'First food of the day (snack)', category: 'SNACK', calories: 380, protein: 6, carbohydrates: 52, fat: 14, fiber: 2, sugars: 32, timestamp: `${selectedDate}T11:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Compensatory mega-lunch', category: 'LUNCH', calories: 1200, protein: 30, carbohydrates: 140, fat: 50, fiber: 5, sugars: 20, timestamp: `${selectedDate}T14:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Afternoon sugar crash snack', category: 'SNACK', calories: 300, protein: 3, carbohydrates: 45, fat: 10, fiber: 1, sugars: 38, timestamp: `${selectedDate}T17:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Late pasta dinner', category: 'DINNER', calories: 850, protein: 18, carbohydrates: 100, fat: 26, fiber: 5, sugars: 8, timestamp: `${selectedDate}T22:30:00`, userId: this.state.user.id, processed: false }
        );
        break;

      // ── S3: True Rapid Meal Cluster (3 meals genuinely within 90 minutes)
      // Fires: CEP Rapid Meal Cluster (anchor 12:00 ± 90 min covers 12:00, 12:40, 13:15)
      //        Rule Binge Pattern Leads to Surplus Risk (cluster + >85% of target)
      //        CEP Calorie Spike (lunch 680 kcal > 50% of some targets)
      case 'rapidCluster':
        journal.meals.push(
          { name: 'Pre-workout snack', category: 'SNACK', calories: 240, protein: 8, carbohydrates: 32, fat: 7, fiber: 2, sugars: 10, timestamp: `${selectedDate}T12:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Rushed lunch', category: 'LUNCH', calories: 720, protein: 25, carbohydrates: 85, fat: 24, fiber: 4, sugars: 10, timestamp: `${selectedDate}T12:40:00`, userId: this.state.user.id, processed: false },
          { name: 'Post-meal dessert', category: 'SNACK', calories: 340, protein: 5, carbohydrates: 48, fat: 14, fiber: 1, sugars: 32, timestamp: `${selectedDate}T13:15:00`, userId: this.state.user.id, processed: false },
          { name: 'Balanced dinner', category: 'DINNER', calories: 560, protein: 38, carbohydrates: 52, fat: 16, fiber: 7, sugars: 4, timestamp: `${selectedDate}T19:00:00`, userId: this.state.user.id, processed: false }
        );
        break;

      // ── S4: Late Night Eating + Circadian Disruption
      // Fires: CEP Late Night Spike (950 kcal at 23:15 > 30% of target) → CIRCADIAN_RHYTHM_DISRUPTION
      //        LateNightEatingPattern inserted directly
      //        CEP Long Fast (12:30 → 17:00 = 4.5h fine, but 17:00 → 23:15 = 6.25h gap) → LONG_FAST_DETECTED
      //        Rule Late Night Plus Weight Loss Conflict (if goal=WEIGHT_LOSS)
      case 'lateNightEating':
        journal.meals.push(
          { name: 'Breakfast', category: 'BREAKFAST', calories: 380, protein: 20, carbohydrates: 45, fat: 10, fiber: 5, sugars: 6, timestamp: `${selectedDate}T08:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Light lunch', category: 'LUNCH', calories: 440, protein: 22, carbohydrates: 52, fat: 12, fiber: 6, sugars: 5, timestamp: `${selectedDate}T12:30:00`, userId: this.state.user.id, processed: false },
          // 6h15m gap from 17:00 dinner to 23:15 snack → LongFast fires
          { name: 'Early snack', category: 'SNACK', calories: 180, protein: 8, carbohydrates: 20, fat: 6, fiber: 2, sugars: 4, timestamp: `${selectedDate}T17:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Huge late-night meal', category: 'DINNER', calories: 980, protein: 14, carbohydrates: 120, fat: 40, fiber: 3, sugars: 30, timestamp: `${selectedDate}T23:15:00`, userId: this.state.user.id, processed: false }
        );
        break;

      // ── S5: Long Fast + Low Protein chain
      // Fires: CEP Long Fast (07:00 breakfast → 17:00 dinner = 10h gap) → LONG_FAST_DETECTED → ENERGY_SHORTAGE
      //        Rule Low Protein Meal (both meals protein < 15g)
      //        PROTEIN_WARNING + PROTEIN_INSUFFICIENT
      case 'longFastLowProtein':
        journal.meals.push(
          { name: 'Minimal toast breakfast', category: 'BREAKFAST', calories: 190, protein: 5, carbohydrates: 32, fat: 4, fiber: 2, sugars: 6, timestamp: `${selectedDate}T07:00:00`, userId: this.state.user.id, processed: false },
          // 10h gap → Long Fast fires
          { name: 'Low-protein pasta dinner', category: 'DINNER', calories: 680, protein: 10, carbohydrates: 92, fat: 20, fiber: 4, sugars: 8, timestamp: `${selectedDate}T17:00:00`, userId: this.state.user.id, processed: false }
        );
        break;

      // ── S6: Unplanned Snacking Overload (4+ SNACK meals in a day)
      // Fires: CEP Unplanned Snacking (4 snacks) → UnplannedSnackingPattern
      //        Rule Snack Overload Caloric Risk (snacking + >80% of target) → SNACK_OVERLOAD_CALORIC_RISK
      //        Rule Caloric Surplus From Snacks (snacking + over target)
      case 'snackOverload':
        journal.meals.push(
          { name: 'Breakfast', category: 'BREAKFAST', calories: 380, protein: 18, carbohydrates: 45, fat: 10, fiber: 4, sugars: 6, timestamp: `${selectedDate}T08:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Snack 1 - biscuits', category: 'SNACK', calories: 290, protein: 3, carbohydrates: 40, fat: 12, fiber: 1, sugars: 22, timestamp: `${selectedDate}T10:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Snack 2 - crisps', category: 'SNACK', calories: 320, protein: 4, carbohydrates: 42, fat: 16, fiber: 1, sugars: 5, timestamp: `${selectedDate}T11:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Light lunch', category: 'LUNCH', calories: 440, protein: 20, carbohydrates: 52, fat: 12, fiber: 5, sugars: 6, timestamp: `${selectedDate}T13:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Snack 3 - chocolate bar', category: 'SNACK', calories: 270, protein: 3, carbohydrates: 36, fat: 13, fiber: 1, sugars: 28, timestamp: `${selectedDate}T15:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Snack 4 - crackers + cheese', category: 'SNACK', calories: 330, protein: 8, carbohydrates: 30, fat: 18, fiber: 2, sugars: 2, timestamp: `${selectedDate}T17:30:00`, userId: this.state.user.id, processed: false }
        );
        break;

      // ── S7: Weekly Breakfast Skip Pattern (3 days no breakfast in current week)
      // Seeds Mon/Tue/Wed with no breakfast → derivedSkippedMeals = 3 MealSkipped facts
      // Fires: CEP Breakfast Skipping Pattern (≥3 MealSkipped) → BreakfastSkippingPattern
      //        CEP Today Breakfast Skipped (today also has no breakfast) → ENERGY_SHORTAGE
      //        Rule Pattern Linkage (ENERGY_SHORTAGE + large meal) → OVEREATING_DETECTED
      //        Rule Energy Shortage from Breakfast Skipping Pattern recommendation
      case 'weeklyBreakfastPattern':
        {
          const start = new Date(this.weekStart());
          for (let i = 0; i < 5; i++) {
            const d = new Date(start);
            d.setDate(start.getDate() + i);
            const key = this.toDateKey(d);
            const j = this.getJournal(key);
            j.meals = [];
            j.skippedMeals = [];
            if (i < 3) {
              // No BREAKFAST meal on first 3 days → derivedSkippedMeals picks these up
              j.meals.push(
                { name: 'Late lunch (no breakfast)', category: 'LUNCH', calories: 900, protein: 24, carbohydrates: 105, fat: 30, fiber: 5, sugars: 12, timestamp: `${key}T14:00:00`, userId: this.state.user.id, processed: false },
                { name: 'Heavy compensatory dinner', category: 'DINNER', calories: 980, protein: 22, carbohydrates: 115, fat: 36, fiber: 5, sugars: 10, timestamp: `${key}T20:30:00`, userId: this.state.user.id, processed: false }
              );
            } else {
              j.meals.push(
                { name: 'Balanced breakfast', category: 'BREAKFAST', calories: 420, protein: 22, carbohydrates: 48, fat: 12, fiber: 5, sugars: 6, timestamp: `${key}T08:00:00`, userId: this.state.user.id, processed: false },
                { name: 'Balanced lunch', category: 'LUNCH', calories: 560, protein: 30, carbohydrates: 60, fat: 14, fiber: 7, sugars: 4, timestamp: `${key}T13:00:00`, userId: this.state.user.id, processed: false }
              );
            }
          }
          // Select first day (no breakfast) so selected day also triggers single-day ENERGY_SHORTAGE
          this.selectedDate.set(this.toDateKey(new Date(start)));
        }
        break;

      // ── S8: Fiber Crisis + Overeating → Compound Nutrition Failure
      // Fires: FIBER_CRITICALLY_LOW (total fiber = 6g < 10g threshold)
      //        Rule Calorie Surplus Direct (total ~2180 kcal > 110% of target)
      //        Rule Overeating Plus Fiber Low Escalation → QUALITY_DEFICIT recommendation
      //        Rule Weight Loss Conflict (if goal=WEIGHT_LOSS)
      case 'fiberCrisis':
        journal.meals.push(
          { name: 'Bagel + cream cheese', category: 'BREAKFAST', calories: 490, protein: 12, carbohydrates: 74, fat: 16, fiber: 1, sugars: 8, timestamp: `${selectedDate}T08:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Fried chicken + white rice', category: 'LUNCH', calories: 950, protein: 38, carbohydrates: 90, fat: 44, fiber: 2, sugars: 6, timestamp: `${selectedDate}T13:00:00`, userId: this.state.user.id, processed: false },
          { name: 'White pasta + sauce', category: 'DINNER', calories: 820, protein: 22, carbohydrates: 108, fat: 22, fiber: 3, sugars: 8, timestamp: `${selectedDate}T19:00:00`, userId: this.state.user.id, processed: false }
        );
        break;

      // ── S9: Balanced optimal day - all signals clear
      case 'balancedDay':
        journal.meals.push(
          { name: 'Oats + berries + egg whites', category: 'BREAKFAST', calories: 310, protein: 26, carbohydrates: 50, fat: 9, fiber: 9, sugars: 12, timestamp: `${selectedDate}T07:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Chicken + rice + salad', category: 'LUNCH', calories: 470, protein: 44, carbohydrates: 58, fat: 13, fiber: 9, sugars: 4, timestamp: `${selectedDate}T12:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Greek yogurt + almonds', category: 'SNACK', calories: 180, protein: 18, carbohydrates: 14, fat: 10, fiber: 2, sugars: 8, timestamp: `${selectedDate}T16:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Salmon + roasted vegetables', category: 'DINNER', calories: 430, protein: 42, carbohydrates: 34, fat: 18, fiber: 9, sugars: 5, timestamp: `${selectedDate}T19:00:00`, userId: this.state.user.id, processed: false }
        );
        break;

      // ── S10: Spec Rule_Pattern_Linkage full showcase
      // Seeds 3 days no breakfast + late-night meals on same days →
      // CEP detects BreakfastSkippingPattern (≥3 MealSkipped) AND LateNightEatingPattern (lateMealCount≥3)
      // Drools: Rule "Pattern Linkage - Breakfast Skipping Causes Late Night Eating" fires →
      //         LINKED_PATTERN_STAGNATION_RISK + Recommendation
      // Also seeds overeating on day 1 → Rule Reactive Eating Confirmed (3-pattern synthesis)
      case 'linkedPatternStagnation':
        {
          const start = new Date(this.weekStart());
          for (let i = 0; i < 7; i++) {
            const d = new Date(start);
            d.setDate(start.getDate() + i);
            const key = this.toDateKey(d);
            const j = this.getJournal(key);
            j.meals = [];
            j.skippedMeals = [];
            if (i < 3) {
              // Days 0-2: no breakfast, large compensatory lunch, very late dinner (after 22h)
              // This creates: 3x MealSkipped(BREAKFAST) → BreakfastSkippingPattern
              //               3x meal at 22:30 → LateNightEatingPattern
              // Day 0 also has a large meal → OVEREATING_DETECTED via pattern linkage
              j.meals.push(
                { name: 'Compensatory lunch (no breakfast)', category: 'LUNCH', calories: 1100, protein: 22, carbohydrates: 125, fat: 38, fiber: 4, sugars: 15, timestamp: `${key}T14:00:00`, userId: this.state.user.id, processed: false },
                { name: 'Late-night binge dinner', category: 'DINNER', calories: 920, protein: 15, carbohydrates: 110, fat: 32, fiber: 3, sugars: 20, timestamp: `${key}T22:30:00`, userId: this.state.user.id, processed: false }
              );
            } else if (i < 5) {
              // Days 3-4: recovery attempt with breakfast but still late snacking
              j.meals.push(
                { name: 'Late breakfast attempt', category: 'BREAKFAST', calories: 380, protein: 16, carbohydrates: 50, fat: 10, fiber: 4, sugars: 8, timestamp: `${key}T09:30:00`, userId: this.state.user.id, processed: false },
                { name: 'Lunch', category: 'LUNCH', calories: 580, protein: 28, carbohydrates: 65, fat: 16, fiber: 6, sugars: 6, timestamp: `${key}T13:30:00`, userId: this.state.user.id, processed: false },
                { name: 'Dinner', category: 'DINNER', calories: 620, protein: 30, carbohydrates: 70, fat: 18, fiber: 7, sugars: 5, timestamp: `${key}T19:00:00`, userId: this.state.user.id, processed: false }
              );
            } else {
              j.meals.push(
                { name: 'Balanced breakfast', category: 'BREAKFAST', calories: 420, protein: 24, carbohydrates: 48, fat: 12, fiber: 6, sugars: 6, timestamp: `${key}T08:00:00`, userId: this.state.user.id, processed: false },
                { name: 'Balanced lunch', category: 'LUNCH', calories: 560, protein: 32, carbohydrates: 62, fat: 14, fiber: 8, sugars: 4, timestamp: `${key}T12:30:00`, userId: this.state.user.id, processed: false }
              );
            }
          }
          // Add weight measurements with near-zero variance to trigger WEIGHT_STAGNATION_CONFIRMED
          // CEP Weight Stagnation fires when weightVarianceKg > 0 && < 0.2
          this.state.weightMeasurements = [];
          const w = this.state.user.weight;
          const startW = new Date(start);
          startW.setDate(start.getDate() - 3);
          this.state.weightMeasurements.push({ userId: this.state.user.id, weight: w + 0.1, timestamp: `${this.toDateKey(startW)}T08:00:00` });
          this.state.weightMeasurements.push({ userId: this.state.user.id, weight: w, timestamp: `${selectedDate}T08:00:00` });

          // Select first day (no breakfast, large meal) to show full chain on selected day
          this.selectedDate.set(this.toDateKey(new Date(start)));
        }
        break;

      // ── S11: Muscle Loss Risk chain (spec Tier 6 multi-day rules)
      // Requires: 5+ DailyIntake facts with protein < 70% target + negative weight trend
      // Seeds: Mon-Fri with very low protein meals + two weight measurements showing loss
      // Drools chain: CEP Consistent Protein Deficit (≥5 days) → ConsistentProteinDeficit fact
      //               Rule Muscle Loss Risk (ConsistentProteinDeficit + WeeklyPattern.weightTrend=NEGATIVE)
      //               → MUSCLE_LOSS_RISK
      //               Rule Protein Critical (MUSCLE_LOSS_RISK + LOW_PROTEIN_MEAL) → PROTEIN_CRITICAL
      //               Rule Compound Nutrition Failure (PROTEIN_CRITICAL + FIBER_CRITICALLY_LOW)
      case 'muscleLossRisk':
        {
          const start = new Date(this.weekStart());
          // Add two weight measurements to create NEGATIVE trend
          this.state.weightMeasurements = [];
          const firstWeightDate = new Date(start);
          firstWeightDate.setDate(start.getDate() - 7);
          this.state.weightMeasurements.push({
            userId: this.state.user.id,
            weight: this.state.user.weight + 1.5,
            timestamp: `${this.toDateKey(firstWeightDate)}T08:00:00`
          });
          this.state.weightMeasurements.push({
            userId: this.state.user.id,
            weight: this.state.user.weight - 0.5,
            timestamp: `${selectedDate}T08:00:00`
          });

          for (let i = 0; i < 7; i++) {
            const d = new Date(start);
            d.setDate(start.getDate() + i);
            const key = this.toDateKey(d);
            const j = this.getJournal(key);
            j.meals = [];
            j.skippedMeals = [];
            if (i < 5) {
              // 5 days chronically low protein (well below 70% of ~101g target = <71g)
              // Each day only gets ~25-35g protein → below 70% of target
              j.meals.push(
                { name: 'Plain oats', category: 'BREAKFAST', calories: 280, protein: 6, carbohydrates: 52, fat: 5, fiber: 3, sugars: 8, timestamp: `${key}T08:00:00`, userId: this.state.user.id, processed: false },
                { name: 'White rice + vegetables', category: 'LUNCH', calories: 420, protein: 8, carbohydrates: 78, fat: 8, fiber: 4, sugars: 6, timestamp: `${key}T13:00:00`, userId: this.state.user.id, processed: false },
                { name: 'Pasta + tomato sauce', category: 'DINNER', calories: 520, protein: 10, carbohydrates: 88, fat: 10, fiber: 2, sugars: 12, timestamp: `${key}T19:00:00`, userId: this.state.user.id, processed: false }
              );
            } else {
              j.meals.push(
                { name: 'Protein breakfast', category: 'BREAKFAST', calories: 450, protein: 30, carbohydrates: 45, fat: 12, fiber: 5, sugars: 8, timestamp: `${key}T08:00:00`, userId: this.state.user.id, processed: false },
                { name: 'Chicken + rice', category: 'LUNCH', calories: 620, protein: 45, carbohydrates: 60, fat: 16, fiber: 6, sugars: 4, timestamp: `${key}T13:00:00`, userId: this.state.user.id, processed: false }
              );
            }
          }
          // Select day 4 (5th low-protein day - the one that triggers the threshold)
          const day4 = new Date(start);
          day4.setDate(start.getDate() + 4);
          this.selectedDate.set(this.toDateKey(day4));
        }
        break;

      // ── S12: Weight Stagnation + Caloric Adaptation Block
      // Requires: WeeklyPattern.weightVarianceKg < 0.2 + averageCalories < target
      // Seeds: 7 days moderate deficit + two nearly-identical weight measurements
      // Drools chain: CEP Weight Stagnation (variance < 0.2) → WeightStagnationDetected
      //               Rule Weight Stagnation Confirmation (WeightStagnationDetected + deficit) → WEIGHT_STAGNATION_CONFIRMED
      //               Rule Extreme Caloric Restriction (averageCalories < 50% BMR) → EXTREME_CALORIC_RESTRICTION
      //               Rule Caloric Adaptation Block (STAGNATION_CONFIRMED + extreme restriction) → CALORIC_ADAPTATION_BLOCK
      //               Rule Plan Change Needed → PLAN_CHANGE_NEEDED (CRITICAL)
      case 'weightStagnation':
        {
          const start = new Date(this.weekStart());
          // Identical weight measurements → variance near 0 → WeightStagnationDetected
          this.state.weightMeasurements = [];
          const wmStart = new Date(start);
          wmStart.setDate(start.getDate() - 2);
          this.state.weightMeasurements.push({
            userId: this.state.user.id,
            weight: this.state.user.weight,
            timestamp: `${this.toDateKey(wmStart)}T08:00:00`
          });
          this.state.weightMeasurements.push({
            userId: this.state.user.id,
            weight: this.state.user.weight + 0.1,
            timestamp: `${selectedDate}T08:00:00`
          });

          for (let i = 0; i < 7; i++) {
            const d = new Date(start);
            d.setDate(start.getDate() + i);
            const key = this.toDateKey(d);
            const j = this.getJournal(key);
            j.meals = [];
            j.skippedMeals = [];
            // Very low calorie days: ~600 kcal/day (below 50% of ~1469 BMR = <735)
            // This triggers EXTREME_CALORIC_RESTRICTION → CALORIC_ADAPTATION_BLOCK → PLAN_CHANGE_NEEDED
            j.meals.push(
              { name: 'Minimal breakfast', category: 'BREAKFAST', calories: 180, protein: 8, carbohydrates: 28, fat: 4, fiber: 2, sugars: 6, timestamp: `${key}T08:00:00`, userId: this.state.user.id, processed: false },
              { name: 'Small salad lunch', category: 'LUNCH', calories: 220, protein: 12, carbohydrates: 22, fat: 10, fiber: 5, sugars: 4, timestamp: `${key}T13:00:00`, userId: this.state.user.id, processed: false },
              { name: 'Light soup dinner', category: 'DINNER', calories: 240, protein: 14, carbohydrates: 28, fat: 7, fiber: 4, sugars: 5, timestamp: `${key}T19:00:00`, userId: this.state.user.id, processed: false }
            );
          }
          // Select last day - all 7 days feed the WeeklyPattern accumulation
          const lastDay = new Date(start);
          lastDay.setDate(start.getDate() + 6);
          this.selectedDate.set(this.toDateKey(lastDay));
        }
        break;

      // ── S13: Extreme Caloric Restriction (standalone daily signal)
      // Just today: intake ~450 kcal (very low) → triggers EXTREME_CALORIC_RESTRICTION from weekly avg
      // Also triggers: ENERGY_SHORTAGE from skipped breakfast, LONG_FAST_DETECTED
      // Frontend note: weekly restriction signal requires WeeklyPattern average below 50% BMR (~735 kcal)
      // This seeds 7 days of low intake so WeeklyPattern triggers the chain.
      case 'extremeRestriction':
        {
          const start = new Date(this.weekStart());
          for (let i = 0; i < 7; i++) {
            const d = new Date(start);
            d.setDate(start.getDate() + i);
            const key = this.toDateKey(d);
            const j = this.getJournal(key);
            j.meals = [];
            j.skippedMeals = [];
            // ~550 kcal per day - well below 50% of BMR (~735)
            j.meals.push(
              { name: 'Black coffee', category: 'BREAKFAST', calories: 5, protein: 0, carbohydrates: 1, fat: 0, fiber: 0, sugars: 0, timestamp: `${key}T07:00:00`, userId: this.state.user.id, processed: false },
              { name: 'Salad no dressing', category: 'LUNCH', calories: 180, protein: 4, carbohydrates: 22, fat: 6, fiber: 4, sugars: 4, timestamp: `${key}T13:00:00`, userId: this.state.user.id, processed: false },
              { name: 'Steamed broccoli + chicken', category: 'DINNER', calories: 310, protein: 28, carbohydrates: 18, fat: 8, fiber: 6, sugars: 4, timestamp: `${key}T19:00:00`, userId: this.state.user.id, processed: false }
            );
          }
          // Select the last day
          const lastDay = new Date(start);
          lastDay.setDate(start.getDate() + 6);
          this.selectedDate.set(this.toDateKey(lastDay));
        }
        break;
    }

    this.journalVersion.update(v => v + 1);
    this.persistState();
    this.evaluateSelectedDay();
  }

  clearSelectedDay(): void {
    this.state.journals[this.selectedDate()] = { date: this.selectedDate(), meals: [], skippedMeals: [] };
    delete this.state.evaluations[this.selectedDate()];
    this.journalVersion.update(v => v + 1);
    this.persistState();
    this.evaluateSelectedDay();
  }

  round(value: number | undefined): number {
    return Math.round(Number(value || 0));
  }

  private buildRequest(): NutritionEvaluationRequest {
    const selectedDate = this.selectedDate();
    const weekJournals = this.weekDays().map((day) => this.getJournal(day.key));
    const dailyIntake = this.toDailyIntake(this.getJournal(selectedDate));
    const previousDailyIntakes = weekJournals
      .filter((j) => j.date !== selectedDate)
      .map((j) => this.toDailyIntake(j));
    const selectedJournal = this.getJournal(selectedDate);

    // Pre-compute targets using the same formula as User.initializeTargets() on the backend.
    const user = this.state.user;
    const bmr = this.calculateBmr(user);
    const goalAdjustment = user.goal === 'WEIGHT_LOSS' ? -500 : user.goal === 'MUSCLE_GAIN' ? 300 : 0;
    const targetCalories = bmr * Number(user.activityFactor) + goalAdjustment;
    const proteinMultiplier = user.goal === 'MUSCLE_GAIN' ? 1.8 : user.goal === 'WEIGHT_LOSS' ? 1.4 : 1.0;
    const targetProtein = Number(user.weight) * proteinMultiplier;
    const targetFiber = user.gender === 'FEMALE' ? 25 : 38;

    // CEP Breakfast Skipping: derive from actual meal data across the week.
    // A day has a skipped breakfast if no BREAKFAST meal is logged that day.
    // This replaces the checkbox - the backend accumulate rule over MealSkipped facts does the pattern detection.
    const derivedSkippedMeals = weekJournals.flatMap((j) => {
      const hasBreakfast = j.meals.some(m => m.category === 'BREAKFAST');
      const hasMeals = j.meals.length > 0;
      if (!hasBreakfast && hasMeals) {
        return [{ userId: user.id, category: 'BREAKFAST' as MealCategory, date: j.date }];
      }
      return [];
    });

    // Also include any explicitly added skipped meals from journal (e.g. from demo scenarios)
    const explicitSkips = weekJournals.flatMap(j => j.skippedMeals);
    const allSkippedMeals = [...derivedSkippedMeals];
    // Merge explicit skips that aren't already covered by derived
    for (const skip of explicitSkips) {
      if (!allSkippedMeals.some(s => s.date === skip.date && s.category === skip.category)) {
        allSkippedMeals.push(skip);
      }
    }

    return {
      user: { ...user, bmr, targetCalories, targetProtein, targetFiber },
      dailyIntake,
      weeklyPattern: this.toWeeklyPattern(weekJournals),
      meals: selectedJournal.meals.map((m) => ({ ...m, processed: false })),
      previousDailyIntakes,
      skippedMeals: allSkippedMeals,
      weightMeasurements: this.state.weightMeasurements,
      activityChanges: [{
        userId: user.id,
        activityFactor: Number(user.activityFactor),
        timestamp: `${selectedDate}T08:00:00`
      } satisfies NewActivityRoutine]
    };
  }

  private toDailyIntake(journal: DayJournal): DailyIntake {
    const totalCalories = journal.meals.reduce((s, m) => s + Number(m.calories || 0), 0);
    return {
      date: journal.date,
      totalCalories,
      averageCalories: totalCalories,
      totalProtein: journal.meals.reduce((s, m) => s + Number(m.protein || 0), 0),
      totalFiber: journal.meals.reduce((s, m) => s + Number(m.fiber || 0), 0),
      mealCount: journal.meals.length,
      userId: this.state.user.id
    };
  }

  private toWeeklyPattern(journals: DayJournal[]): WeeklyPattern {
    const intakes = journals.map((j) => this.toDailyIntake(j));
    const totalCalories = intakes.reduce((s, i) => s + i.totalCalories, 0);
    // Count derived breakfast skips (days with meals but no BREAKFAST) + explicit skips
    const derivedSkipCount = journals.filter(j => j.meals.length > 0 && !j.meals.some(m => m.category === 'BREAKFAST')).length;
    const explicitSkipCount = journals.reduce((s, j) => s + j.skippedMeals.length, 0);
    const totalSkips = derivedSkipCount + journals.reduce((s, j) => {
      // Add explicit skips not already covered by derived
      const hasBreakfast = j.meals.some(m => m.category === 'BREAKFAST');
      const derivedAlreadyCounted = !hasBreakfast && j.meals.length > 0;
      return s + j.skippedMeals.filter(sk => !derivedAlreadyCounted || sk.category !== 'BREAKFAST').length;
    }, 0);
    return {
      weekId: this.toDateKey(this.weekStart()),
      skippedMealCount: totalSkips,
      lateMealCount: journals.flatMap((j) => j.meals).filter((m) => {
        const h = new Date(m.timestamp).getHours();
        return h >= 22 || h < 6;
      }).length,
      totalCalories,
      averageCalories: totalCalories / 7,
      weightTrend: this.calculateWeightTrend(),
      proteinTrend: intakes.reduce((s, i) => s + i.totalProtein, 0) / 7,
      userId: this.state.user.id,
      weightVarianceKg: this.calculateWeightVariance()
    };
  }

  private calculateWeightVariance(): number {
    const weights = this.state.weightMeasurements.map((m) => Number(m.weight));
    if (weights.length < 2) return 0;
    return Math.max(...weights) - Math.min(...weights);
  }

  private calculateWeightTrend(): WeightTrend {
    const weights = this.state.weightMeasurements;
    if (weights.length < 2) return 'STABLE';
    const first = weights[0].weight;
    const last = weights[weights.length - 1].weight;
    if (last < first - 0.2) return 'NEGATIVE';
    if (last > first + 0.2) return 'POSITIVE';
    return 'STABLE';
  }

  private calculateBmr(user: UserProfile): number {
    const base = (10 * Number(user.weight)) + (6.25 * Number(user.height)) - (5 * Number(user.age));
    return user.gender === 'MALE' ? base + 5 : base - 161;
  }

  private getJournal(date: string): DayJournal {
    if (!this.state.journals[date]) {
      this.state.journals[date] = { date, meals: [], skippedMeals: [] };
    }
    return this.state.journals[date];
  }

  private emptyMealForm(): MealForm {
    return { name: '', category: 'BREAKFAST', time: '08:00', calories: 450, protein: 25, carbohydrates: 45, fat: 14, fiber: 6, sugars: 8 };
  }

  private defaultState(): AppState {
    return {
      user: { id: 10, gender: 'FEMALE', age: 28, weight: 72, height: 168, goal: 'WEIGHT_LOSS', activityFactor: 1.375, allergies: [] },
      journals: {},
      weightMeasurements: [],
      evaluations: {}
    };
  }

  private restoreState(): void {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (!stored) return;
    try {
      this.state = JSON.parse(stored) as AppState;
      this.state.journals = this.state.journals || {};
      this.state.weightMeasurements = this.state.weightMeasurements || [];
      this.state.evaluations = this.state.evaluations || {};
    } catch {
      this.state = this.defaultState();
    }
  }

  private persistState(): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this.state));
  }

  private getWeekStart(date: Date): Date {
    const start = new Date(date);
    const day = start.getDay() || 7;
    start.setDate(start.getDate() - day + 1);
    start.setHours(0, 0, 0, 0);
    return start;
  }

  private toDateKey(date: Date): string {
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
