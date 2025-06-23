package com.nutri_sci.service;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.Meal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SwapEngine contains the logic for finding and swapping food items within a meal
 * and recalculating its nutritional properties based on user-defined goals.
 */
public class SwapEngine {

    private final NutrientCalculator nutrientCalculator;
    private final DBManager dbManager;

    // Thresholds defining what constitutes a "normal" or "significant" nutritional change.
    private static final double NORMAL_THRESHOLD = 0.10; // 10%
    private static final double SIGNIFICANT_THRESHOLD = 0.25; // 25%
    // Tolerance for how much other (non-target) nutrients are allowed to change.
    private static final double OTHER_NUTRIENT_TOLERANCE = 0.8; // 80%

    // Pattern to parse lines like "100g chicken" to separate quantity from description.
    private final Pattern ingredientPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*g\\s*(.+)", Pattern.CASE_INSENSITIVE);

    public SwapEngine() {
        this.nutrientCalculator = new NutrientCalculator();
        this.dbManager = DBManager.getInstance();
    }

    /**
     * Finds reasonable food swaps based on user-defined nutritional goals.
     * It prioritizes suggestions from the same food group to provide more relevant results.
     */
    public List<String> findSwaps(Meal originalMeal, String itemToSwap, String nutrient, String goalType, String intensity) {
        Matcher matcher = ingredientPattern.matcher(itemToSwap.trim());
        if (!matcher.matches()) {
            System.err.println("ERROR: Could not parse the ingredient to be swapped: " + itemToSwap);
            return new ArrayList<>();
        }
        // Extract just the food name (e.g., "chicken") from the full ingredient line.
        String originalDescription = matcher.group(2).trim();

        Map<String, Double> originalNutrients = dbManager.getNutrientProfile(originalDescription);
        if (originalNutrients.isEmpty()) {
            System.err.println("WARN: Could not find nutrient profile for original item: " + originalDescription);
            return new ArrayList<>();
        }

        // Map the user's goal ("Increase"/"Decrease") to the database query rank ("HIGH"/"LOW").
        String rank = goalType.equals("Increase") ? "HIGH" : "LOW";
        List<String> potentialSwaps = dbManager.getFoodsByNutrientRank(nutrient, rank);

        // Use a stream to efficiently filter the large list of potential swaps.
        List<String> filteredSwaps = potentialSwaps.stream()
                // Ensure an item isn't suggested as a swap for itself.
                .filter(potentialSwap -> !potentialSwap.equalsIgnoreCase(originalDescription))
                // The main filter: checks if a swap meets the specific nutritional goal and tolerance.
                .filter(potentialSwap -> {
                    Map<String, Double> newNutrients = dbManager.getNutrientProfile(potentialSwap);
                    if (newNutrients.isEmpty()) {
                        return false;
                    }
                    return meetsGoal(originalNutrients, newNutrients, nutrient, goalType, intensity);
                })
                .collect(Collectors.toList());

        // Get the food group of the original item to prioritize and label suggestions.
        String originalFoodGroup = dbManager.getFoodGroup(itemToSwap);

        // Separate suggestions into two lists: same food group and others.
        List<String> sameGroupSuggestions = new ArrayList<>();
        List<String> otherGroupSuggestions = new ArrayList<>();

        if (originalFoodGroup != null) {
            for (String swap : filteredSwaps) {
                // A dummy quantity is added to allow the getFoodGroup method to parse the string.
                String currentSwapFoodGroup = dbManager.getFoodGroup("100g " + swap);
                if (originalFoodGroup.equals(currentSwapFoodGroup)) {
                    sameGroupSuggestions.add(swap + " (same food group)");
                } else {
                    otherGroupSuggestions.add(swap);
                }
            }
        } else {
            // If the original item has no food group, all suggestions go into the "other" list.
            otherGroupSuggestions.addAll(filteredSwaps);
        }

        // Combine the lists, with same-group suggestions appearing first for relevance.
        List<String> finalSuggestions = new ArrayList<>();
        finalSuggestions.addAll(sameGroupSuggestions);
        finalSuggestions.addAll(otherGroupSuggestions);

        return finalSuggestions;
    }

    /**
     * Helper method to determine if a swap is valid based on nutritional goals and tolerances.
     */
    private boolean meetsGoal(Map<String, Double> originalNutrients, Map<String, Double> newNutrients, String nutrient, String goalType, String intensity) {
        double originalValue = originalNutrients.getOrDefault(nutrient, 0.0);
        double newValue = newNutrients.getOrDefault(nutrient, 0.0);

        // Handle cases where the original nutrient value is zero.
        if (originalValue <= 0) {
            return goalType.equals("Increase") && newValue > 0;
        }

        double percentChange = (newValue - originalValue) / originalValue;
        // Select the appropriate threshold based on the user's desired "intensity".
        double threshold = intensity.contains("significant") ? SIGNIFICANT_THRESHOLD : NORMAL_THRESHOLD;
        boolean goalMet;

        if (goalType.equals("Increase")) {
            goalMet = percentChange >= threshold;
        } else { // Decrease
            goalMet = percentChange <= -threshold;
        }

        if (!goalMet) return false;

        // After confirming the primary goal is met, check that other nutrients don't change too drastically.
        for (String key : originalNutrients.keySet()) {
            if (!key.equals(nutrient)) {
                double originalOther = originalNutrients.get(key);
                if (originalOther == 0) continue; // Skip if there's nothing to compare against.

                double replacementOther = newNutrients.getOrDefault(key, 0.0);
                double otherPercentChange = (replacementOther - originalOther) / originalOther;

                if (goalType.equals("Decrease")) {
                    // When decreasing a nutrient, we mainly want to avoid other nutrients INCREASING too much.
                    if (otherPercentChange > OTHER_NUTRIENT_TOLERANCE) {
                        return false;
                    }
                } else { // When increasing, be stricter: don't let other nutrients change too much in either direction.
                    if (Math.abs(otherPercentChange) > OTHER_NUTRIENT_TOLERANCE) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Creates a new Meal object with the swapped ingredient.
     */
    public Meal performSwap(Meal originalMeal, String itemToSwap, String newItem) {
        Meal swappedMeal = new Meal();
        swappedMeal.setDate(originalMeal.getDate());
        swappedMeal.setMealType(originalMeal.getMealType());

        String originalIngredients = originalMeal.getIngredients();
        // Preserve the quantity (e.g., "150g") from the original item to apply to the new item.
        String quantity = itemToSwap.split("g\\s+")[0] + "g ";
        String newIngredientLine = quantity + newItem;

        String swappedIngredients = originalIngredients.replace(itemToSwap, newIngredientLine);
        swappedMeal.setIngredients(swappedIngredients);

        // Recalculate the total estimated calories for the newly constituted meal.
        double newCalories = nutrientCalculator.calculateCaloriesForMeal(swappedIngredients);
        swappedMeal.setEstimatedCalories(newCalories);

        return swappedMeal;
    }
}