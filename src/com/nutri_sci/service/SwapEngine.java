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
 * <p>
 * The SwapEngine analyzes meals and suggests healthier food alternatives based on
 * user-defined nutritional goals. It employs a sophisticated scoring algorithm that
 * balances goal achievement with nutritional stability, ensuring that recommended
 * swaps improve targeted nutrients while minimizing disruption to other nutritional
 * values.
 * </p>
 * <p>
 * The engine supports both relative (percentage-based) and absolute goal targeting,
 * and can optionally restrict suggestions to foods within the same food group for
 * better culinary compatibility.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.model.Goal
 * @see com.nutri_sci.model.SwapSuggestion
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

    /**
     * Constructs a new SwapEngine with the required dependencies.
     * 
     * @param nutrientCalculator service for calculating meal nutritional values
     * @param dbManager database manager for accessing food and nutrition data
     */
    public SwapEngine(NutrientCalculator nutrientCalculator, DBManager dbManager) {
        this.nutrientCalculator = nutrientCalculator;
        this.dbManager = dbManager;
    }

    /**
     * Finds and evaluates potential food swaps for a specific ingredient in a meal.
     * <p>
     * This method analyzes the specified ingredient and returns a list of ranked
     * swap suggestions that help achieve the user's nutritional goals. The algorithm
     * considers goal achievement, nutritional stability, and food group compatibility
     * when scoring potential swaps.
     * </p>
     * <p>
     * The method supports various filtering options:
     * <ul>
     *   <li><strong>sameGroupOnly</strong>: Restricts swaps to the same food group</li>
     *   <li><strong>strictTolerance</strong>: Excludes swaps that exceed stability tolerance</li>
     *   <li><strong>tolerance</strong>: Maximum allowed deviation for non-target nutrients</li>
     * </ul>
     * </p>
     * 
     * @param originalMeal the meal containing the ingredient to swap
     * @param itemToSwap the ingredient to replace (format: "100g chicken breast")
     * @param goals list of nutritional goals to optimize for
     * @param tolerance maximum percentage deviation allowed for non-target nutrients (e.g., 20.0 for 20%)
     * @param sameGroupOnly if true, only consider swaps within the same food group
     * @param strictTolerance if true, exclude any swaps that exceed the tolerance
     * @return list of swap suggestions ranked by score (best first), limited to top 20
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

            // Calculate both absolute and percentage changes scaled by ingredient quantity
            Map<String, Double> nutrientChanges = new HashMap<>();
            Map<String, Double> nutrientPercentChanges = new HashMap<>();
            Set<String> allNutrientKeys = new HashSet<>(originalItemNutrients.keySet());
            allNutrientKeys.addAll(newItemNutrients.keySet());

            for (String nutrient : allNutrientKeys) {
                // Values must be scaled by the quantity of the ingredient in the meal
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

    /**
     * Calculates scoring metrics for a potential food swap.
     * <p>
     * This method evaluates how well a potential swap achieves the user's goals
     * while maintaining nutritional stability. It returns both the total error
     * score and stability penalty to enable flexible filtering of results.
     * </p>
     * 
     * @param originalNutrients nutritional profile of the original ingredient
     * @param newNutrients nutritional profile of the potential replacement
     * @param goals list of nutritional goals to optimize for
     * @param tolerance maximum percentage deviation allowed for non-target nutrients
     * @return array containing [finalScore, stabilityPenalty] where lower scores are better
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
     * <p>
     * This method generates a new Meal object by replacing the specified ingredient
     * with the new item while maintaining the same quantity. The nutritional values
     * are recalculated for the entire meal with the swap applied.
     * </p>
     * 
     * @param originalMeal the original meal to modify
     * @param itemToSwap the ingredient to replace (format: "100g chicken breast")
     * @param newItem the replacement ingredient description (just the name, quantity is preserved)
     * @return a new Meal object with the swap applied and updated nutritional values
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
     * <p>
     * This helper method searches for the calorie entry in the nutritional data,
     * handling potential variations in the calorie field naming.
     * </p>
     * 
     * @param nutrients map of nutritional values
     * @return the calorie value, or 0.0 if not found
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