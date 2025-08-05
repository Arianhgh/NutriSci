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

/**
 * Core service responsible for finding and evaluating food swap recommendations.
 * Refactored to use extracted methods for better maintainability.
 */
public class SwapEngine {

    /** Service for calculating nutritional values */
    private final NutrientCalculator nutrientCalculator;
    
    /** Database manager for accessing food and nutrition data */
    private final DBManager dbManager;
    
    /** Regular expression pattern for parsing ingredient quantities and descriptions */
    private final Pattern ingredientPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*g\\s*(.+)", Pattern.CASE_INSENSITIVE);

    /** Weight factor for goal achievement in the scoring algorithm */
    private static final double GOAL_ACHIEVEMENT_WEIGHT = 100.0;
    
    /** Weight factor for nutritional stability penalty in the scoring algorithm */
    private static final double NUTRITIONAL_STABILITY_WEIGHT = 50.0;
    
    /** Bonus score for swaps within the same food group */
    private static final double FOOD_GROUP_BONUS = -20.0;

    public SwapEngine(NutrientCalculator nutrientCalculator, DBManager dbManager) {
        this.nutrientCalculator = nutrientCalculator;
        this.dbManager = dbManager;
    }

    /**
     * Finds and evaluates potential food swaps for a specific ingredient in a meal.
     * Refactored to use extracted methods for better readability and maintainability.
     */
    public List<SwapSuggestion> findSwaps(Meal originalMeal, String itemToSwap, List<Goal> goals, double tolerance, boolean sameGroupOnly, boolean strictTolerance) {
        Matcher matcher = ingredientPattern.matcher(itemToSwap.trim());
        if (!matcher.matches() || goals.isEmpty()) {
            return new ArrayList<>();
        }

        double originalQuantity = Double.parseDouble(matcher.group(1));
        String originalDescription = matcher.group(2).trim();
        Map<String, Double> originalItemNutrients = dbManager.getNutrientProfile(originalDescription);
        if (originalItemNutrients.isEmpty()) return new ArrayList<>();

        // Get potential swap items
        Set<String> potentialSwapsSet = getPotentialSwapItems(goals, itemToSwap, sameGroupOnly);
        if (potentialSwapsSet.isEmpty()) return new ArrayList<>();

        // Score and filter suggestions
        List<SwapSuggestion> scoredSuggestions = scoreSuggestions(potentialSwapsSet, originalDescription, 
                originalItemNutrients, goals, tolerance, strictTolerance, originalQuantity, itemToSwap);

        return filterSuggestions(scoredSuggestions);
    }

    /**
     * Gets potential swap items based on goals and constraints.
     * Extracted method to reduce complexity in findSwaps.
     */
    private Set<String> getPotentialSwapItems(List<Goal> goals, String itemToSwap, boolean sameGroupOnly) {
        Set<String> potentialSwapsSet = new HashSet<>();
        String originalFoodGroup = dbManager.getFoodGroup(itemToSwap);

        if (sameGroupOnly) {
            if (originalFoodGroup != null) {
                potentialSwapsSet.addAll(dbManager.getFoodsFromGroup(originalFoodGroup));
            }
        } else {
            for (Goal goal : goals) {
                String rank = goal.getType().equals("Increase") ? "HIGH" : "LOW";
                potentialSwapsSet.addAll(dbManager.getFoodsByNutrientRank(goal.getNutrient(), rank));
            }
            if (originalFoodGroup != null) {
                potentialSwapsSet.addAll(dbManager.getFoodsFromGroup(originalFoodGroup));
            }
        }

        return potentialSwapsSet;
    }

    /**
     * Scores potential suggestions and filters based on goals and constraints.
     * Extracted method to reduce complexity in findSwaps.
     */
    private List<SwapSuggestion> scoreSuggestions(Set<String> potentialSwapsSet, String originalDescription,
            Map<String, Double> originalItemNutrients, List<Goal> goals, double tolerance, 
            boolean strictTolerance, double originalQuantity, String itemToSwap) {
        
        List<SwapSuggestion> scoredSuggestions = new ArrayList<>();
        String originalFoodGroup = dbManager.getFoodGroup(itemToSwap);

        for (String potentialSwap : new ArrayList<>(potentialSwapsSet)) {
            if (potentialSwap.equalsIgnoreCase(originalDescription)) continue;

            Map<String, Double> newItemNutrients = dbManager.getNutrientProfile(potentialSwap);
            if (newItemNutrients.isEmpty()) continue;

            // Check if swap moves in correct direction for goals
            if (!movesInCorrectDirection(originalItemNutrients, newItemNutrients, goals)) {
                continue;
            }

            // Calculate scores
            double[] scores = calculateSwapScores(originalItemNutrients, newItemNutrients, goals, tolerance);
            double finalScore = scores[0];
            double stabilityPenalty = scores[1];

            if (strictTolerance && stabilityPenalty > 0) continue;

            // Apply food group bonus
            String swapFoodGroup = dbManager.getFoodGroup("100g " + potentialSwap);
            if (swapFoodGroup != null && swapFoodGroup.equals(originalFoodGroup)) {
                finalScore += FOOD_GROUP_BONUS;
            }

            // Calculate nutrient changes
            Map<String, Double> nutrientChanges = new HashMap<>();
            Map<String, Double> nutrientPercentChanges = new HashMap<>();
            calculateNutrientChanges(originalItemNutrients, newItemNutrients, originalQuantity, 
                    nutrientChanges, nutrientPercentChanges);

            scoredSuggestions.add(new SwapSuggestion(potentialSwap, swapFoodGroup, finalScore, 
                    nutrientChanges, nutrientPercentChanges));
        }

        return scoredSuggestions;
    }

    /**
     * Checks if a potential swap moves in the correct direction for all goals.
     * Extracted method to improve readability.
     */
    private boolean movesInCorrectDirection(Map<String, Double> originalNutrients, 
            Map<String, Double> newNutrients, List<Goal> goals) {
        for (Goal goal : goals) {
            double actualChange = newNutrients.getOrDefault(goal.getNutrient(), 0.0) - 
                    originalNutrients.getOrDefault(goal.getNutrient(), 0.0);
            if ((goal.getType().equals("Increase") && actualChange < 0) || 
                (goal.getType().equals("Decrease") && actualChange > 0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates absolute and percentage changes for all nutrients.
     * Extracted method to reduce complexity in the main scoring loop.
     */
    private void calculateNutrientChanges(Map<String, Double> originalNutrients, 
            Map<String, Double> newNutrients, double originalQuantity,
            Map<String, Double> nutrientChanges, Map<String, Double> nutrientPercentChanges) {
        
        Set<String> allNutrientKeys = new HashSet<>(originalNutrients.keySet());
        allNutrientKeys.addAll(newNutrients.keySet());

        for (String nutrient : allNutrientKeys) {
            // Values must be scaled by the quantity of the ingredient in the meal
            double originalValTotal = originalNutrients.getOrDefault(nutrient, 0.0) * (originalQuantity / 100.0);
            double newValTotal = newNutrients.getOrDefault(nutrient, 0.0) * (originalQuantity / 100.0);

            nutrientChanges.put(nutrient, newValTotal - originalValTotal);

            if (originalValTotal != 0) {
                nutrientPercentChanges.put(nutrient, (newValTotal - originalValTotal) / originalValTotal);
            } else {
                nutrientPercentChanges.put(nutrient, newValTotal > 0 ? 1.0 : 0.0);
            }
        }
    }

    /**
     * Filters and sorts suggestions to return the top results.
     * Extracted method to improve clarity.
     */
    private List<SwapSuggestion> filterSuggestions(List<SwapSuggestion> scoredSuggestions) {
        return scoredSuggestions.stream()
                .sorted(Comparator.comparingDouble(SwapSuggestion::getFinalScore))
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * Calculates scoring metrics for a potential food swap.
     */
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

    /**
     * Creates a new meal with a specified ingredient swap applied.
     */
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

    /**
     * Extracts the calorie value from a nutritional profile map.
     */
    private double getCalorieValue(Map<String, Double> nutrients) {
        for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
            if (entry.getKey().toUpperCase().startsWith("ENERGY (KILOCALORIES)")) {
                return entry.getValue();
            }
        }
        return 0.0;
    }
}