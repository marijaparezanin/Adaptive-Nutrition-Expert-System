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
  FIBER_CRITICALLY_LOW: 'Fiber Critical'
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
  endDayLoading = signal(false);

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
    if (!ev || !ev.analyticSeverities?.length) return 'ok';
    if (ev.analyticSeverities.includes('CRITICAL')) return 'critical';
    if (ev.analyticSeverities.includes('HIGH')) return 'high';
    if (ev.analyticSeverities.includes('WARNING')) return 'warning';
    return 'ok';
  }

  get expertStatusLabel(): string {
    const ev = this.evaluation();
    if (!ev) return 'Awaiting input';
    if (ev.analyticSeverities?.includes('CRITICAL')) return 'Critical alert';
    if (ev.analyticSeverities?.includes('HIGH')) return 'Attention needed';
    if (ev.analyticStatuses?.length) return 'Signals detected';
    if (ev.dailyStatus) return ev.dailyStatus.replace(/_/g, ' ');
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

  /** Filter out idle boilerplate text — only show if no real signals exist */
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

  /** Simulate end-of-day pseudo-clock: detect which main meal categories were NOT logged */
  endDay(): void {
    this.endDayLoading.set(true);
    const journal = this.selectedJournal;
    const loggedCategories = new Set(journal.meals.map(m => m.category));
    const missedAlerts: MissedMealAlert[] = [];
    const now = new Date();
    const h = now.getHours();

    if (!loggedCategories.has('BREAKFAST') && !journal.skippedMeals.some(s => s.category === 'BREAKFAST')) {
      missedAlerts.push({ userId: this.state.user.id, missedCategory: 'BREAKFAST', hoursPastExpected: Math.max(0, h - 9) });
    }
    if (!loggedCategories.has('LUNCH') && h >= 14) {
      missedAlerts.push({ userId: this.state.user.id, missedCategory: 'LUNCH', hoursPastExpected: Math.max(0, h - 13) });
    }

    this.nutritionApi.evaluate({ ...this.buildRequest(), missedMealAlerts: missedAlerts }).subscribe({
      next: (response) => {
        this.evaluation.set(response);
        this.state.evaluations[this.selectedDate()] = response;
        this.persistState();
        this.endDayLoading.set(false);
      },
      error: () => {
        this.apiError.set('Backend not reachable.');
        this.endDayLoading.set(false);
      }
    });
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

  seedExpertScenario(): void {
    const start = new Date(this.weekStart());
    for (let index = 0; index < 3; index++) {
      const date = new Date(start);
      date.setDate(start.getDate() + index);
      const key = this.toDateKey(date);
      const journal = this.getJournal(key);
      journal.skippedMeals = [{ userId: this.state.user.id, category: 'BREAKFAST', date: key }];
      journal.meals.push({
        name: 'Late high-calorie dinner',
        category: 'DINNER',
        calories: 950,
        protein: 14,
        carbohydrates: 110,
        fat: 36,
        fiber: 5,
        sugars: 30,
        timestamp: `${key}T22:45:00`,
        userId: this.state.user.id,
        processed: false
      });
    }
    this.selectedDate.set(this.toDateKey(start));
    this.journalVersion.update(v => v + 1);
    this.persistState();
    this.evaluateSelectedDay();
  }

  loadTestScenario(scenario: string): void {
    const selectedDate = this.selectedDate();
    const journal = this.selectedJournal;
    journal.meals = [];
    journal.skippedMeals = [];

    switch (scenario) {
      case 'lateNightBinge':
        journal.skippedMeals = [{ userId: this.state.user.id, category: 'BREAKFAST', date: selectedDate }];
        journal.meals.push(
          { name: 'Large late dinner', category: 'DINNER', calories: 1200, protein: 35, carbohydrates: 150, fat: 45, fiber: 8, sugars: 40, timestamp: `${selectedDate}T23:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Late night snack', category: 'SNACK', calories: 400, protein: 8, carbohydrates: 55, fat: 16, fiber: 2, sugars: 35, timestamp: `${selectedDate}T23:50:00`, userId: this.state.user.id, processed: false }
        );
        break;

      case 'breakfastSkipping':
        journal.skippedMeals = [{ userId: this.state.user.id, category: 'BREAKFAST', date: selectedDate }];
        journal.meals.push(
          { name: 'Mid-morning snack', category: 'SNACK', calories: 200, protein: 6, carbohydrates: 25, fat: 8, fiber: 3, sugars: 12, timestamp: `${selectedDate}T10:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Compensatory lunch', category: 'LUNCH', calories: 940, protein: 28, carbohydrates: 110, fat: 30, fiber: 6, sugars: 12, timestamp: `${selectedDate}T14:20:00`, userId: this.state.user.id, processed: false },
          { name: 'Sweet snack', category: 'SNACK', calories: 260, protein: 4, carbohydrates: 40, fat: 9, fiber: 2, sugars: 28, timestamp: `${selectedDate}T17:05:00`, userId: this.state.user.id, processed: false },
          { name: 'Late pasta dinner', category: 'DINNER', calories: 830, protein: 19, carbohydrates: 95, fat: 22, fiber: 5, sugars: 8, timestamp: `${selectedDate}T22:35:00`, userId: this.state.user.id, processed: false }
        );
        break;

      case 'eatingLessGoal':
        journal.meals.push(
          { name: 'Small breakfast', category: 'BREAKFAST', calories: 250, protein: 10, carbohydrates: 30, fat: 8, fiber: 4, sugars: 8, timestamp: `${selectedDate}T08:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Light lunch', category: 'LUNCH', calories: 350, protein: 15, carbohydrates: 42, fat: 12, fiber: 5, sugars: 6, timestamp: `${selectedDate}T12:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Light dinner', category: 'DINNER', calories: 300, protein: 12, carbohydrates: 35, fat: 10, fiber: 4, sugars: 5, timestamp: `${selectedDate}T18:00:00`, userId: this.state.user.id, processed: false }
        );
        break;

      case 'highProteinDay':
        journal.meals.push(
          { name: 'Protein pancakes', category: 'BREAKFAST', calories: 450, protein: 35, carbohydrates: 45, fat: 12, fiber: 4, sugars: 8, timestamp: `${selectedDate}T07:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Protein shake', category: 'SNACK', calories: 250, protein: 30, carbohydrates: 20, fat: 5, fiber: 1, sugars: 8, timestamp: `${selectedDate}T10:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Grilled chicken with rice', category: 'LUNCH', calories: 650, protein: 45, carbohydrates: 70, fat: 15, fiber: 5, sugars: 2, timestamp: `${selectedDate}T12:45:00`, userId: this.state.user.id, processed: false },
          { name: 'Protein bar', category: 'SNACK', calories: 200, protein: 20, carbohydrates: 18, fat: 6, fiber: 3, sugars: 5, timestamp: `${selectedDate}T16:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Salmon with vegetables', category: 'DINNER', calories: 550, protein: 40, carbohydrates: 35, fat: 22, fiber: 6, sugars: 3, timestamp: `${selectedDate}T19:00:00`, userId: this.state.user.id, processed: false }
        );
        break;

      case 'bingePlusSnacking':
        journal.skippedMeals = [{ userId: this.state.user.id, category: 'BREAKFAST', date: selectedDate }];
        journal.meals.push(
          { name: 'Late lunch after skipping', category: 'LUNCH', calories: 780, protein: 22, carbohydrates: 90, fat: 28, fiber: 5, sugars: 15, timestamp: `${selectedDate}T14:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Stress snack 1', category: 'SNACK', calories: 280, protein: 5, carbohydrates: 38, fat: 12, fiber: 1, sugars: 22, timestamp: `${selectedDate}T16:30:00`, userId: this.state.user.id, processed: false },
          { name: 'Stress snack 2', category: 'SNACK', calories: 310, protein: 4, carbohydrates: 42, fat: 14, fiber: 1, sugars: 28, timestamp: `${selectedDate}T17:00:00`, userId: this.state.user.id, processed: false },
          { name: 'Stress snack 3', category: 'SNACK', calories: 260, protein: 3, carbohydrates: 36, fat: 11, fiber: 1, sugars: 20, timestamp: `${selectedDate}T17:25:00`, userId: this.state.user.id, processed: false },
          { name: 'Late night delivery', category: 'DINNER', calories: 1100, protein: 28, carbohydrates: 130, fat: 42, fiber: 6, sugars: 18, timestamp: `${selectedDate}T23:00:00`, userId: this.state.user.id, processed: false }
        );
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
    // Sending these explicitly ensures Drools receives correct values even if the
    // "Initialize user target values" rule fires after rules that depend on them.
    const user = this.state.user;
    const bmr = this.calculateBmr(user);
    const goalAdjustment = user.goal === 'WEIGHT_LOSS' ? -500 : user.goal === 'MUSCLE_GAIN' ? 300 : 0;
    const targetCalories = bmr * Number(user.activityFactor) + goalAdjustment;
    const proteinMultiplier = user.goal === 'MUSCLE_GAIN' ? 1.8 : user.goal === 'WEIGHT_LOSS' ? 1.4 : 1.0;
    const targetProtein = Number(user.weight) * proteinMultiplier;
    const targetFiber = user.gender === 'FEMALE' ? 25 : 38;

    return {
      user: { ...user, bmr, targetCalories, targetProtein, targetFiber },
      dailyIntake,
      weeklyPattern: this.toWeeklyPattern(weekJournals),
      meals: selectedJournal.meals.map((m) => ({ ...m, processed: false })),
      previousDailyIntakes,
      skippedMeals: selectedJournal.skippedMeals,
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
    return {
      weekId: this.toDateKey(this.weekStart()),
      skippedMealCount: journals.reduce((s, j) => s + j.skippedMeals.length, 0),
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
