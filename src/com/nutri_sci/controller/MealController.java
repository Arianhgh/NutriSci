package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.MealDataNotifier;
import com.nutri_sci.service.NutrientCalculator;

import javax.swing.*;
import java.util.Date;

/**
 * Handles business logic related to meal logging and interacts with the database.
 */
public class MealController {
    private final DBManager dbManager;
    private final NutrientCalculator nutrientCalculator;

    public MealController() {
        // Use Singleton pattern to get the database manager instance.
        this.dbManager = DBManager.getInstance();
        this.nutrientCalculator = new NutrientCalculator();
    }

    /**
     * Validates and logs a meal for a specific user.
     *
     * @param user        The user profile logging the meal.
     * @param date        The date of the meal.
     * @param mealType    The type of meal (e.g., Breakfast, Lunch).
     * @param ingredients A string listing the meal's ingredients.
     * @return True if the meal was logged successfully, false otherwise.
     */
    public boolean logMeal(UserProfile user, Date date, String mealType, String ingredients) {
        // Basic input validation.
        if (mealType == null || ingredients.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Meal type and ingredients are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Business Rule: A user can't log the same meal type (except "Snack") twice on the same day.
        if (!mealType.equals("Snack") && dbManager.hasUserLoggedMealTypeOnDate(user.getId(), mealType, date)) {
            JOptionPane.showMessageDialog(null, "You have already logged a " + mealType + " for that day.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Create and populate a new Meal object.
        Meal meal = new Meal();
        meal.setDate(date);
        meal.setMealType(mealType);
        meal.setIngredients(ingredients);
        // Calculate nutrient estimates before saving.
        meal.setEstimatedCalories(nutrientCalculator.calculateCaloriesForMeal(ingredients));

        boolean success = dbManager.saveMeal(user.getId(), meal);
        if (success) {
            JOptionPane.showMessageDialog(null, "Meal logged successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            // Using Observer pattern to notify other parts of the application (e.g., UI) of the data change.
            MealDataNotifier.getInstance().notifyObserversOfChange();
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Failed to log meal.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}