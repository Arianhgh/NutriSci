package com.nutri_sci.model;

import com.nutri_sci.service.NutrientCalculator;

import java.util.Date;
import java.util.Map;

/**
 * Represents a logged meal with its nutritional information and metadata.
 * Enhanced with factory method for better encapsulation of meal creation logic.
 */
public class Meal {
    
    /** Unique database identifier for the meal */
    private int mealId;
    
    /** Date when the meal was consumed */
    private Date date;
    
    /** Type of meal (e.g., "Breakfast", "Lunch", "Dinner", "Snack") */
    private String mealType;
    
    /** Raw ingredient list as entered by the user (format: "100g chicken breast\n200g rice") */
    private String ingredients;
    
    /** Total estimated calories for the meal */
    private double estimatedCalories;
    
    /** Detailed breakdown of nutrients (key: nutrient name, value: amount) */
    private Map<String, Double> nutrientBreakdown;
    
    /** Flag indicating whether this meal is a result of a food swap recommendation */
    private boolean isSwapped = false;
    
    /** Reference to the original meal ID if this is a swapped version */
    private Integer originalMealId = null;

    /**
     * Static factory method to create a new meal with calculated nutritional values.
     * This method encapsulates the meal creation logic that was previously in MealController,
     * improving cohesion by keeping meal-related operations in the Meal class.
     * 
     * @param date the date when the meal was consumed
     * @param mealType the type of meal (e.g., "Breakfast", "Lunch", "Dinner", "Snack")
     * @param verifiedIngredients the verified ingredient list
     * @param nutrientCalculator the service for calculating nutritional values
     * @return a new Meal object with calculated nutritional values
     */
    public static Meal createNewMeal(Date date, String mealType, String verifiedIngredients, 
                                   NutrientCalculator nutrientCalculator) {
        Meal meal = new Meal();
        meal.setDate(date);
        meal.setMealType(mealType);
        meal.setIngredients(verifiedIngredients);

        Map<String, Double> nutrients = nutrientCalculator.calculateNutrientsForMeal(verifiedIngredients);
        meal.setEstimatedCalories(extractCalorieValue(nutrients));
        meal.setNutrientBreakdown(nutrients);

        return meal;
    }

    /**
     * Helper method to extract the calorie value from the comprehensive nutrient map.
     * This makes the retrieval logic robust against changes in nutrient naming.
     *
     * @param nutrients The map of all nutrients calculated for a meal.
     * @return The total kilocalories for the meal.
     */
    private static double extractCalorieValue(Map<String, Double> nutrients) {
        for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
            // Check for the official name for calories from the database.
            if (entry.getKey().toUpperCase().startsWith("ENERGY (KILOCALORIES)")) {
                return entry.getValue();
            }
        }
        return 0.0;
    }

    /**
     * Gets the unique meal identifier.
     * 
     * @return the meal database ID
     */
    public int getMealId() { return mealId; }
    
    /**
     * Sets the unique meal identifier.
     * 
     * @param mealId the meal database ID
     */
    public void setMealId(int mealId) { this.mealId = mealId; }
    
    /**
     * Gets the date when the meal was consumed.
     * 
     * @return the meal date
     */
    public Date getDate() { return date; }
    
    /**
     * Sets the date when the meal was consumed.
     * 
     * @param date the meal date
     */
    public void setDate(Date date) { this.date = date; }
    
    /**
     * Gets the meal type.
     * 
     * @return the meal type (e.g., "Breakfast", "Lunch", "Dinner", "Snack")
     */
    public String getMealType() { return mealType; }
    
    /**
     * Sets the meal type.
     * 
     * @param mealType the meal type (e.g., "Breakfast", "Lunch", "Dinner", "Snack")
     */
    public void setMealType(String mealType) { this.mealType = mealType; }
    
    /**
     * Gets the raw ingredients string as entered by the user.
     * 
     * @return the ingredients in format "100g chicken breast\n200g rice"
     */
    public String getIngredients() { return ingredients; }
    
    /**
     * Sets the raw ingredients string.
     * 
     * @param ingredients the ingredients in format "100g chicken breast\n200g rice"
     */
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    
    /**
     * Gets the total estimated calories for the meal.
     * 
     * @return the total calories
     */
    public double getEstimatedCalories() { return estimatedCalories; }
    
    /**
     * Sets the total estimated calories for the meal.
     * 
     * @param estimatedCalories the total calories
     */
    public void setEstimatedCalories(double estimatedCalories) { this.estimatedCalories = estimatedCalories; }
    
    /**
     * Gets the detailed nutrient breakdown for the meal.
     * 
     * @return a map of nutrient names to their amounts (e.g., "Protein" -> 25.5)
     */
    public Map<String, Double> getNutrientBreakdown() { return nutrientBreakdown; }
    
    /**
     * Sets the detailed nutrient breakdown for the meal.
     * 
     * @param nutrientBreakdown a map of nutrient names to their amounts
     */
    public void setNutrientBreakdown(Map<String, Double> nutrientBreakdown) { this.nutrientBreakdown = nutrientBreakdown; }
    
    /**
     * Checks if this meal is the result of a food swap recommendation.
     * 
     * @return true if this meal replaces an original meal, false otherwise
     */
    public boolean isSwapped() { return isSwapped; }
    
    /**
     * Sets whether this meal is the result of a food swap recommendation.
     * 
     * @param swapped true if this meal replaces an original meal, false otherwise
     */
    public void setSwapped(boolean swapped) { isSwapped = swapped; }
    
    /**
     * Gets the ID of the original meal if this is a swapped version.
     * 
     * @return the original meal ID, or null if this is not a swapped meal
     */
    public Integer getOriginalMealId() { return originalMealId; }
    
    /**
     * Sets the ID of the original meal if this is a swapped version.
     * 
     * @param originalMealId the original meal ID, or null if this is not a swapped meal
     */
    public void setOriginalMealId(Integer originalMealId) { this.originalMealId = originalMealId; }
}