package com.nutri_sci.service;

import com.nutri_sci.database.DBManager;
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

/**
 * SwapEngine contains the logic for finding and swapping food items within a meal
 * and recalculating its nutritional properties based on user-defined goals.
 * This version uses a sophisticated scoring system to rank suggestions.
 */
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

    public List<SwapSuggestion> findSwaps(Meal originalMeal, String itemToSwap, String goalNutrient, String goalType, double goalValue, boolean isRelative, double tolerance, boolean sameGroupOnly, boolean strictTolerance) {
        Matcher matcher = ingredientPattern.matcher(itemToSwap.trim());
        if (!matcher.matches()) {
            System.err.println("ERROR: Could not parse the ingredient: " + itemToSwap);
            return new ArrayList<>();
        }

        double originalQuantity = Double.parseDouble(matcher.group(1));
        String originalDescription = matcher.group(2).trim();
        Map<String, Double> originalItemNutrients = dbManager.getNutrientProfile(originalDescription);
        if (originalItemNutrients.isEmpty()) return new ArrayList<>();

        // --- Candidate Gathering ---
        String originalFoodGroup = dbManager.getFoodGroup(itemToSwap);
        List<String> potentialSwaps;

        // If sameGroupOnly is checked, the candidate list is restricted from the start.
        if (sameGroupOnly) {
            if (originalFoodGroup != null) {
                potentialSwaps = dbManager.getFoodsFromGroup(originalFoodGroup);
            } else {
                // If the original item has no food group, we cannot fulfill this request.
                return new ArrayList<>();
            }
        } else {
            String rank = goalType.equals("Increase") ? "HIGH" : "LOW";
            List<String> potentialSwapsByRank = dbManager.getFoodsByNutrientRank(goalNutrient, rank);
            List<String> potentialSwapsByGroup = new ArrayList<>();
            if (originalFoodGroup != null) {
                potentialSwapsByGroup = dbManager.getFoodsFromGroup(originalFoodGroup);
            }
            Set<String> combinedSet = new HashSet<>(potentialSwapsByRank);
            combinedSet.addAll(potentialSwapsByGroup);
            potentialSwaps = new ArrayList<>(combinedSet);
        }

        List<SwapSuggestion> scoredSuggestions = new ArrayList<>();
        for (String potentialSwap : potentialSwaps) {
            if (potentialSwap.equalsIgnoreCase(originalDescription)) continue;

            Map<String, Double> newItemNutrients = dbManager.getNutrientProfile(potentialSwap);
            if (newItemNutrients.isEmpty()) continue;

            // This is a hard filter. A suggestion must move in the correct direction.
            double actualChange = newItemNutrients.getOrDefault(goalNutrient, 0.0) - originalItemNutrients.getOrDefault(goalNutrient, 0.0);
            if (goalType.equals("Increase") && actualChange < 0) {
                continue; // If goal is to increase, don't show items that decrease it.
            }
            if (goalType.equals("Decrease") && actualChange > 0) {
                continue; // If goal is to decrease, don't show items that increase it.
            }

            double[] scores = calculateSwapScores(originalItemNutrients, newItemNutrients, goalNutrient, goalType, goalValue, isRelative, tolerance);
            double finalScore = scores[0];
            double stabilityPenalty = scores[1];

            if (strictTolerance && stabilityPenalty > 0) {
                continue;
            }

            // The food group bonus is now a tie-breaker among valid suggestions.
            String swapFoodGroup = dbManager.getFoodGroup("100g " + potentialSwap);
            if (swapFoodGroup != null && swapFoodGroup.equals(originalFoodGroup)) {
                finalScore += FOOD_GROUP_BONUS;
            }

            Map<String, Double> nutrientChanges = new HashMap<>();
            Map<String, Double> nutrientPercentChanges = new HashMap<>();
            Set<String> allNutrientKeys = new HashSet<>(originalItemNutrients.keySet());
            allNutrientKeys.addAll(newItemNutrients.keySet());
            for (String nutrient : allNutrientKeys) {
                double originalVal = originalItemNutrients.getOrDefault(nutrient, 0.0) * (originalQuantity / 100.0);
                double newVal = newItemNutrients.getOrDefault(nutrient, 0.0) * (originalQuantity / 100.0);
                nutrientChanges.put(nutrient, newVal - originalVal);
                if (originalVal != 0) {
                    nutrientPercentChanges.put(nutrient, (newVal - originalVal) / originalVal);
                } else {
                    nutrientPercentChanges.put(nutrient, newVal > 0 ? 1.0 : 0.0);
                }
            }
            scoredSuggestions.add(new SwapSuggestion(potentialSwap, swapFoodGroup, finalScore, nutrientChanges, nutrientPercentChanges));
        }

        return scoredSuggestions.stream()
                .sorted(Comparator.comparingDouble(SwapSuggestion::getFinalScore))
                .limit(20)
                .collect(Collectors.toList());
    }

    private double[] calculateSwapScores(Map<String, Double> originalNutrients, Map<String, Double> newNutrients, String goalNutrient, String goalType, double goalValue, boolean isRelative, double tolerance) {
        double originalTargetValue = originalNutrients.getOrDefault(goalNutrient, 0.0);
        double idealChange = isRelative ? originalTargetValue * (goalValue / 100.0) : goalValue;
        if (goalType.equals("Decrease")) idealChange *= -1;

        double newTargetValue = newNutrients.getOrDefault(goalNutrient, 0.0);
        double actualChange = newTargetValue - originalTargetValue;
        double goalError = Math.abs(actualChange - idealChange);
        double goalAchievementScore = goalError;

        double totalDeviation = 0;
        int nutrientCount = 0;
        Set<String> allNutrientKeys = new HashSet<>(originalNutrients.keySet());
        allNutrientKeys.addAll(newNutrients.keySet());
        for (String nutrient : allNutrientKeys) {
            if (nutrient.equals(goalNutrient)) continue;
            double originalVal = originalNutrients.getOrDefault(nutrient, 0.0);
            double newVal = newNutrients.getOrDefault(nutrient, 0.0);
            if (originalVal > 0) {
                double percentDeviation = Math.abs((newVal - originalVal) / originalVal);
                if (percentDeviation > (tolerance / 100.0)) {
                    totalDeviation += (percentDeviation - (tolerance / 100.0));
                }
            } else if (newVal > 0) {
                totalDeviation += 1.0;
            }
            nutrientCount++;
        }
        double nutritionalStabilityPenalty = (nutrientCount > 0) ? totalDeviation : 0;
        double finalScore = (goalAchievementScore * GOAL_ACHIEVEMENT_WEIGHT) + (nutritionalStabilityPenalty * NUTRITIONAL_STABILITY_WEIGHT);

        return new double[]{finalScore, nutritionalStabilityPenalty};
    }

    public Meal performSwap(Meal originalMeal, String itemToSwap, String newItem) {
        Meal swappedMeal = new Meal();
        swappedMeal.setDate(originalMeal.getDate());
        swappedMeal.setMealType(originalMeal.getMealType());

        String originalIngredients = originalMeal.getIngredients();
        String quantity = itemToSwap.split("g\\s+")[0] + "g ";
        String newIngredientLine = quantity + newItem;

        String swappedIngredients = originalIngredients.replace(itemToSwap, newIngredientLine);
        swappedMeal.setIngredients(swappedIngredients);

        Map<String, Double> newNutrients = nutrientCalculator.calculateNutrientsForMeal(swappedIngredients);
        swappedMeal.setEstimatedCalories(newNutrients.getOrDefault("Calories", 0.0));
        swappedMeal.setNutrientBreakdown(newNutrients);

        return swappedMeal;
    }
}