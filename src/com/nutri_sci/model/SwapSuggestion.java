package com.nutri_sci.model;

import java.util.Map;

/**
 * A data-holding class that represents a single food swap suggestion.
 * It contains the suggested food item and a detailed breakdown of the
 * nutritional changes that would occur if the swap were made.
 */
public class SwapSuggestion {

    private final String foodName;
    private final String foodGroup;
    private final double finalScore;
    private final Map<String, Double> nutrientChanges; // Key: Nutrient Name, Value: Change Amount
    private final Map<String, Double> nutrientPercentChanges; // Key: Nutrient Name, Value: % Change

    public SwapSuggestion(String foodName, String foodGroup, double finalScore, Map<String, Double> nutrientChanges, Map<String, Double> nutrientPercentChanges) {
        this.foodName = foodName;
        this.foodGroup = foodGroup;
        this.finalScore = finalScore;
        this.nutrientChanges = nutrientChanges;
        this.nutrientPercentChanges = nutrientPercentChanges;
    }

    // Getters
    public String getFoodName() { return foodName; }
    public String getFoodGroup() { return foodGroup; }
    public double getFinalScore() { return finalScore; }
    public Map<String, Double> getNutrientChanges() { return nutrientChanges; }
    public Map<String, Double> getNutrientPercentChanges() { return nutrientPercentChanges; }

    /**
     * This will be used to render the suggestion in the UI.
     */
    @Override
    public String toString() {
        return String.format("<html><b>%s</b><br>" +
                        "<font size='-2'>Calories: %+.2f kcal (%+.1f%%) | Protein: %+.2fg | Fiber: %+.2fg</font></html>",
                foodName,
                nutrientChanges.getOrDefault("Calories", 0.0),
                nutrientPercentChanges.getOrDefault("Calories", 0.0) * 100,
                nutrientChanges.getOrDefault("Protein", 0.0),
                nutrientChanges.getOrDefault("Fiber", 0.0)
        );
    }
}