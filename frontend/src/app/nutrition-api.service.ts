import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type Gender = 'MALE' | 'FEMALE';
export type GoalType = 'WEIGHT_LOSS' | 'MAINTENANCE' | 'MUSCLE_GAIN';
export type MealCategory = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK';
export type WeightTrend = 'NEGATIVE' | 'STABLE' | 'POSITIVE';
export type SeverityLevel = 'LOW' | 'WARNING' | 'HIGH' | 'CRITICAL';

export interface UserProfile {
  id: number;
  gender: Gender;
  age: number;
  weight: number;
  height: number;
  goal: GoalType;
  activityFactor: number;
  allergies: string[];
  targetCalories?: number;
  targetProtein?: number;
  targetFiber?: number;
  bmr?: number;
}

export interface MealEntry {
  name: string;
  category: MealCategory;
  calories: number;
  protein: number;
  carbohydrates: number;
  fat: number;
  fiber: number;
  sugars: number;
  timestamp: string;
  userId: number;
  processed?: boolean;
}

export interface DailyIntake {
  date: string;
  totalCalories: number;
  averageCalories: number;
  totalProtein: number;
  totalFiber: number;
  mealCount: number;
  userId: number;
}

export interface SkippedMeal {
  userId: number;
  category: MealCategory;
  date: string;
}

export interface WeightMeasured {
  userId: number;
  weight: number;
  timestamp: string;
}

export interface NewActivityRoutine {
  userId: number;
  activityFactor: number;
  timestamp: string;
}

export interface WeeklyPattern {
  weekId: string;
  skippedMealCount: number;
  lateMealCount: number;
  totalCalories: number;
  averageCalories: number;
  weightTrend: WeightTrend;
  proteinTrend: number;
  userId: number;
  weightVarianceKg: number;
}

export interface MissedMealAlert {
  userId: number;
  missedCategory: string;
  hoursPastExpected: number;
  detectedAt?: string;
  description?: string;
}

export interface NutritionEvaluationRequest {
  user: UserProfile;
  dailyIntake: DailyIntake;
  weeklyPattern: WeeklyPattern;
  meals: MealEntry[];
  previousDailyIntakes: DailyIntake[];
  skippedMeals: SkippedMeal[];
  weightMeasurements: WeightMeasured[];
  activityChanges: NewActivityRoutine[];
  missedMealAlerts?: MissedMealAlert[];
}

export interface NutritionEvaluationResponse {
  firedRules: number;
  bmr: number;
  targetCalories: number;
  targetProtein: number;
  targetFiber: number;
  totalCalories: number;
  totalProtein: number;
  totalFiber: number;
  caloriesRemaining: number;
  proteinRemaining: number;
  fiberRemaining: number;
  mealCount: number;
  dailyStatus?: string;
  analyticStatuses: string[];
  analyticSeverities: string[];
  recommendations: string[];
  recommendationTypes: string[];
  detectedPatterns: string[];
  patternDetails: string[];
  expertOpinionSummary: string;
  reasoningTrace: string[];
  calorieStatusReason: string;
  proteinStatusReason: string;
  fiberStatusReason: string;
  caloriePercentage: number;
  proteinPercentage: number;
  fiberPercentage: number;
  firedRuleNames?: string[];
}

@Injectable({ providedIn: 'root' })
export class NutritionApiService {
  private readonly apiUrl = 'http://localhost:8080/api/nutrition/evaluate';

  constructor(private readonly http: HttpClient) {}

  evaluate(request: NutritionEvaluationRequest): Observable<NutritionEvaluationResponse> {
    return this.http.post<NutritionEvaluationResponse>(this.apiUrl, request);
  }
}
