package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.MealDataNotifier;
import com.nutri_sci.service.NutrientCalculator;
import com.nutri_sci.service.SwapEngine;

import javax.swing.*;
import java.util.Date;
import java.util.List;

/**
 * Controller for managing food swap operations and meal replacements.
 * <p>
 * The SwapController orchestrates the complete food swap process, from applying
 * the swap recommendation to persisting the new meal and notifying the UI of
 * changes. It integrates the SwapEngine functionality with the application's
 * data persistence and notification systems.
 * </p>
 * <p>
 * Key responsibilities include:
 * <ul>
 *   <li>Coordinating food swap operations using the SwapEngine</li>
 *   <li>Managing the persistence of swapped meals</li>
 *   <li>Maintaining relationships between original and swapped meals</li>
 *   <li>Notifying observers of meal data changes</li>
 *   <li>Providing user feedback for swap operations</li>
 * </ul>
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.service.SwapEngine
 * @see com.nutri_sci.model.Meal
 */
public class SwapController {
    
    /** Database manager for meal persistence operations */
    private final DBManager dbManager;
    
    /** Swap engine for performing food swap calculations and meal generation */
    private final SwapEngine swapEngine;

    /**
     * Constructs a new SwapController with necessary dependencies.
     * <p>
     * Initializes the controller with database access and creates a configured
     * SwapEngine with the required nutritional calculation capabilities.
     * </p>
     */
    public SwapController() {
        this.dbManager = DBManager.getInstance();
        NutrientCalculator nutrientCalculator = new NutrientCalculator(this.dbManager);
        this.swapEngine = new SwapEngine(nutrientCalculator, this.dbManager);
    }

    /**
     * Performs a food swap, saves the resulting meal, and notifies observers.
     * <p>
     * This method orchestrates the complete swap process:
     * <ol>
     *   <li>Uses the SwapEngine to generate a new meal with the swap applied</li>
     *   <li>Marks the meal as a swapped version and links it to the original</li>
     *   <li>Persists the new meal to the database</li>
     *   <li>Notifies UI components of the data change</li>
     *   <li>Provides user feedback on the operation result</li>
     * </ol>
     * </p>
     * 
     * @param user the current user profile for meal ownership
     * @param originalMeal the meal being replaced with a healthier version
     * @param itemToSwap the specific ingredient line to be replaced (format: "100g chicken breast")
     * @param newItem the description of the replacement food item
     * @return the newly created and saved Meal object, or null if the operation failed
     */
    public Meal performAndSaveSwap(UserProfile user, Meal originalMeal, String itemToSwap, String newItem) {
        // Use the SwapEngine to create a new Meal object with the swapped item
        Meal swappedMeal = swapEngine.performSwap(originalMeal, itemToSwap, newItem);

        // Set properties to link it to the original meal
        swappedMeal.setSwapped(true);
        swappedMeal.setOriginalMealId(originalMeal.getMealId());

        // Save the new, swapped meal to the database
        boolean success = dbManager.saveMeal(user.getId(), swappedMeal);

        if (success) {
            // Notify the main UI to refresh the meal journal using the modern notifier
            MealDataNotifier.getInstance().notifyMealDataChanged();
            JOptionPane.showMessageDialog(null, "Swap completed and saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return swappedMeal;
        } else {
            JOptionPane.showMessageDialog(null, "Failed to save the swapped meal.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Applies a swap to all relevant meals within a given date range.
     * @param user The current user profile.
     * @param itemToSwap The ingredient line to be replaced.
     * @param newItem The description of the new food item.
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     */
    public void applySwapOverTime(UserProfile user, String itemToSwap, String newItem, Date startDate, Date endDate) {
        List<Meal> meals = dbManager.getMealsForUser(user.getId(), startDate, endDate);
        int swapCount = 0;

        for (Meal meal : meals) {
            if (meal.getIngredients().contains(itemToSwap)) {
                Meal swappedMeal = swapEngine.performSwap(meal, itemToSwap, newItem);
                swappedMeal.setSwapped(true);
                swappedMeal.setOriginalMealId(meal.getMealId());
                if (dbManager.saveMeal(user.getId(), swappedMeal)) {
                    swapCount++;
                }
            }
        }

        if (swapCount > 0) {
            MealDataNotifier.getInstance().notifyMealDataChanged();
            JOptionPane.showMessageDialog(null, "Successfully applied the swap to " + swapCount + " past meal(s).", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "No past meals found containing the item to swap.", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}