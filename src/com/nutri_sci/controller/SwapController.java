package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.MealDataNotifier;
import com.nutri_sci.service.SwapEngine;

import javax.swing.*;

/**
 * Controller to manage the food swap process, including saving the new meal
 * and notifying the UI of the change.
 */
public class SwapController {
    private final DBManager dbManager;
    private final SwapEngine swapEngine;

    public SwapController() {
        this.dbManager = DBManager.getInstance();
        this.swapEngine = new SwapEngine();
    }

    /**
     * Performs the swap, saves the new meal to the database, and notifies observers.
     * @param user The current user profile.
     * @param originalMeal The meal being replaced.
     * @param itemToSwap The ingredient line to be replaced.
     * @param newItem The description of the new food item.
     * @return The newly created and saved Meal object, or null on failure.
     */
    public Meal performAndSaveSwap(UserProfile user, Meal originalMeal, String itemToSwap, String newItem) {
        // Use the SwapEngine to create a new Meal object with the swapped item.
        Meal swappedMeal = swapEngine.performSwap(originalMeal, itemToSwap, newItem);

        // Set properties to link it to the original meal.
        swappedMeal.setSwapped(true);
        swappedMeal.setOriginalMealId(originalMeal.getMealId());

        // Save the new, swapped meal to the database.
        boolean success = dbManager.saveMeal(user.getId(), swappedMeal);

        if (success) {
            // Notify the main UI to refresh the meal journal.
            MealDataNotifier.getInstance().notifyObserversOfChange();
            JOptionPane.showMessageDialog(null, "Swap completed and saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return swappedMeal;
        } else {
            JOptionPane.showMessageDialog(null, "Failed to save the swapped meal.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}