package com.nutri_sci.service;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.Goal;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.SwapSuggestion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SwapEngine {

    private final NutrientCalculator nutrientCalculator;
    private final DBManager dbManager;
    private final Pattern ingredientPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*g\\s*(.+)", Pattern.CASE_INSENSITIVE);

    private static final double GOAL_ACHIEVEMENT_WEIGHT = 100.0;
    private static final double NUTRITIONAL_STABILITY_WEIGHT = 50.0;
    private static final double FOOD_GROUP_BONUS = -20.0;

    public SwapEngine() {
        this.nutrientCalculator = new NutrientCalculator();
        this.dbManager = DBManager.getInstance();
    }

    public List<SwapSuggestion> findSwaps(Meal originalMeal, String itemToSwap, List<Goal> goals, double tolerance, boolean sameGroupOnly, boolean strictTolerance) {
        Matcher matcher = ingredientPattern.matcher(itemToSwap.trim());
        if (!matcher.matches() || goals.isEmpty()) {
            return new ArrayList<>();
        }

        double originalQuantity = Double.parseDouble(matcher.group(1));
        String originalDescription = matcher.group(2).trim();
        Map<String, Double> originalItemNutrients = dbManager.getNutrientProfile(originalDescription);
        if (originalItemNutrients.isEmpty()) return new ArrayList<>();

        Set<String> potentialSwapsSet = new HashSet<>();
        String originalFoodGroup = dbManager.getFoodGroup(itemToSwap);

        if (sameGroupOnly) {
            if (originalFoodGroup != null) potentialSwapsSet.addAll(dbManager.getFoodsFromGroup(originalFoodGroup));
            else return new ArrayList<>();
        } else {
            for (Goal goal : goals) {
                String rank = goal.getType().equals("Increase") ? "HIGH" : "LOW";
                potentialSwapsSet.addAll(dbManager.getFoodsByNutrientRank(goal.getNutrient(), rank));
            }
            if (originalFoodGroup != null) potentialSwapsSet.addAll(dbManager.getFoodsFromGroup(originalFoodGroup));
        }

        List<SwapSuggestion> scoredSuggestions = new ArrayList<>();
        for (String potentialSwap : new ArrayList<>(potentialSwapsSet)) {
            if (potentialSwap.equalsIgnoreCase(originalDescription)) continue;

            Map<String, Double> newItemNutrients = dbManager.getNutrientProfile(potentialSwap);
            if (newItemNutrients.isEmpty()) continue;

            boolean movesInCorrectDirection = true;
            for (Goal goal : goals) {
                double actualChange = newItemNutrients.getOrDefault(goal.getNutrient(), 0.0) - originalItemNutrients.getOrDefault(goal.getNutrient(), 0.0);
                if ((goal.getType().equals("Increase") && actualChange < 0) || (goal.getType().equals("Decrease") && actualChange > 0)) {
                    movesInCorrectDirection = false;
                    break;
                }
            }
            if (!movesInCorrectDirection) continue;

            double[] scores = calculateSwapScores(originalItemNutrients, newItemNutrients, goals, tolerance);
            double finalScore = scores[0];
            double stabilityPenalty = scores[1];

            if (strictTolerance && stabilityPenalty > 0) continue;

            String swapFoodGroup = dbManager.getFoodGroup("100g " + potentialSwap);
            if (swapFoodGroup != null && swapFoodGroup.equals(originalFoodGroup)) {
                finalScore += FOOD_GROUP_BONUS;
            }

            // **FIXED AREA**: Correctly calculate both absolute and percentage changes.
            Map<String, Double> nutrientChanges = new HashMap<>();
            Map<String, Double> nutrientPercentChanges = new HashMap<>();
            Set<String> allNutrientKeys = new HashSet<>(originalItemNutrients.keySet());
            allNutrientKeys.addAll(newItemNutrients.keySet());

            for (String nutrient : allNutrientKeys) {
                // Values must be scaled by the quantity of the ingredient in the meal.
                double originalValTotal = originalItemNutrients.getOrDefault(nutrient, 0.0) * (originalQuantity / 100.0);
                double newValTotal = newItemNutrients.getOrDefault(nutrient, 0.0) * (originalQuantity / 100.0);

                nutrientChanges.put(nutrient, newValTotal - originalValTotal);

                if (originalValTotal != 0) {
                    nutrientPercentChanges.put(nutrient, (newValTotal - originalValTotal) / originalValTotal);
                } else {
                    nutrientPercentChanges.put(nutrient, newValTotal > 0 ? 1.0 : 0.0);
                }
            }

            scoredSuggestions.add(new SwapSuggestion(potentialSwap, swapFoodGroup, finalScore, nutrientChanges, nutrientPercentChanges));
        }

        return scoredSuggestions.stream()
                .sorted(Comparator.comparingDouble(SwapSuggestion::getFinalScore))
                .limit(20)
                .collect(Collectors.toList());
    }

    private double[] calculateSwapScores(Map<String, Double> originalNutrients, Map<String, Double> newNutrients, List<Goal> goals, double tolerance) {
        double totalGoalError = 0;
        for (Goal goal : goals) {
            double originalVal = originalNutrients.getOrDefault(goal.getNutrient(), 0.0);
            double idealChange = goal.isRelative() ? originalVal * (goal.getValue() / 100.0) : goal.getValue();
            if (goal.getType().equals("Decrease")) idealChange *= -1;

            double actualChange = newNutrients.getOrDefault(goal.getNutrient(), 0.0) - originalVal;
            totalGoalError += Math.abs(actualChange - idealChange);
        }

        double stabilityPenalty = 0;
        Set<String> goalNutrients = goals.stream().map(Goal::getNutrient).collect(Collectors.toSet());
        Set<String> allNutrients = new HashSet<>(originalNutrients.keySet());
        allNutrients.addAll(newNutrients.keySet());

        for (String nutrient : allNutrients) {
            if (goalNutrients.contains(nutrient)) continue;
            double originalVal = originalNutrients.getOrDefault(nutrient, 0.0);
            double newVal = newNutrients.getOrDefault(nutrient, 0.0);
            if (originalVal > 0) {
                double deviation = Math.abs((newVal - originalVal) / originalVal);
                if (deviation > (tolerance / 100.0)) {
                    stabilityPenalty += (deviation - (tolerance / 100.0));
                }
            } else if (newVal > 0) {
                stabilityPenalty += 1.0;
            }
        }

        double finalScore = (totalGoalError * GOAL_ACHIEVEMENT_WEIGHT) + (stabilityPenalty * NUTRITIONAL_STABILITY_WEIGHT);
        return new double[]{finalScore, stabilityPenalty};
    }

    public Meal performSwap(Meal originalMeal, String itemToSwap, String newItem) {
        Meal swappedMeal = new Meal();
        swappedMeal.setDate(originalMeal.getDate());
        swappedMeal.setMealType(originalMeal.getMealType());

        String quantity = itemToSwap.split("g\\s+")[0] + "g ";
        String newIngredientLine = quantity + newItem;

        String swappedIngredients = originalMeal.getIngredients().replace(itemToSwap, newIngredientLine);
        swappedMeal.setIngredients(swappedIngredients);

        Map<String, Double> newNutrients = nutrientCalculator.calculateNutrientsForMeal(swappedIngredients);
        swappedMeal.setEstimatedCalories(getCalorieValue(newNutrients));
        swappedMeal.setNutrientBreakdown(newNutrients);

        return swappedMeal;
    }

    private double getCalorieValue(Map<String, Double> nutrients) {
        for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
            if (entry.getKey().toUpperCase().startsWith("ENERGY (KILOCALORIES)")) {
                return entry.getValue();
            }
        }
        return 0.0;
    }
}