package com.nutri_sci.service;

import com.nutri_sci.database.DBManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This service class encapsulates business logic for nutrient calculations.
 * connects to the database to calculate calories based on the
 * Canadian Nutrient File data
 */
public class NutrientCalculator {

    private final DBManager dbManager;
    // Regex to parse lines like "150g chicken breast" or "150 g chicken breast"
    private final Pattern ingredientPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*g\\s*(.+)", Pattern.CASE_INSENSITIVE);

    public NutrientCalculator() {
        this.dbManager = DBManager.getInstance();
    }

    /**
     * Calculates total calories for a meal by looking up each ingredient in the database.
     * It parses an ingredient list where each line is expected to be in the format:
     * "[amount]g [description]" (e.g., "150g chicken").
     *
     * @param ingredients A string containing ingredients, one per line from the MealLoggingUI.
     * @return The calculated total calorie count for the meal.
     */
    public double calculateCaloriesForMeal(String ingredients) {
        double totalCalories = 0;
        if (ingredients == null || ingredients.trim().isEmpty()) {
            return 0;
        }

        String[] ingredientLines = ingredients.split("\\n");

        for (String line : ingredientLines) {
            if (line.trim().isEmpty()) {
                continue;
            }

            Matcher matcher = ingredientPattern.matcher(line.trim());

            if (matcher.matches()) {
                try {
                    // Step 1: Parse the ingredient amount and description
                    double grams = Double.parseDouble(matcher.group(1));
                    String description = matcher.group(2).trim();

                    // Step 2: Find the ingredient in the database to get its FoodID
                    int foodId = dbManager.findFoodId(description);

                    if (foodId != -1) {
                        // Step 3: Retrieve the base nutrient data (calories per 100g)
                        double caloriesPer100g = dbManager.getCaloriesPer100g(foodId);

                        // Step 4: Calculate the adjusted nutrient amount for the specific weight
                        double ingredientCalories = (caloriesPer100g / 100.0) * grams;
                        totalCalories += ingredientCalories;
                    } else {
                        System.err.println("Ingredient not found in DB: '" + description + "'. Skipping.");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse weight from line: " + line);
                }
            } else {
                System.err.println("Could not parse ingredient line: '" + line + "'. Expected format: '[amount]g [description]'");
            }
        }

        // Step 5: Return the aggregated total for the entire meal
        return totalCalories;
    }
}