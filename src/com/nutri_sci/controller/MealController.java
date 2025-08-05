package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.FoodItem;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.MealDataNotifier;
import com.nutri_sci.service.NutrientCalculator;
import com.nutri_sci.ui.IngredientResolverDialog;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for managing meal-related business logic and operations.
 * Refactored to use Meal factory method for better separation of concerns.
 */
public class MealController {
    
    /** Database manager for data persistence operations */
    private final DBManager dbManager;
    
    /** Service for calculating nutritional values */
    private final NutrientCalculator nutrientCalculator;
    
    /** Regular expression pattern for parsing ingredient quantities and descriptions */
    private final Pattern ingredientPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*g\\s*(.+)", Pattern.CASE_INSENSITIVE);

    public MealController(DBManager dbManager, NutrientCalculator nutrientCalculator) {
        this.dbManager = dbManager;
        this.nutrientCalculator = nutrientCalculator;
    }

    /**
     * Validates and logs a meal with interactive ingredient resolution.
     * Refactored to use Meal.createNewMeal() factory method for better encapsulation.
     */
    public boolean logMeal(UserProfile user, Date date, String mealType, String rawIngredients, JFrame owner) {
        if (mealType == null || rawIngredients.trim().isEmpty()) {
            JOptionPane.showMessageDialog(owner, "Meal type and ingredients are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!mealType.equals("Snack") && dbManager.hasUserLoggedMealTypeOnDate(user.getId(), mealType, date)) {
            JOptionPane.showMessageDialog(owner, "You have already logged a " + mealType + " for this day.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            // Resolve ingredients before proceeding
            String verifiedIngredients = resolveIngredients(rawIngredients, owner);
            if (verifiedIngredients == null) {
                return false; // User cancelled or an error occurred
            }

            // Use the factory method from Meal class for better encapsulation
            Meal meal = Meal.createNewMeal(date, mealType, verifiedIngredients, nutrientCalculator);

            if (dbManager.saveMeal(user.getId(), meal)) {
                JOptionPane.showMessageDialog(owner, "Meal logged successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                MealDataNotifier.getInstance().notifyMealDataChanged();
                return true;
            } else {
                JOptionPane.showMessageDialog(owner, "Failed to log meal.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(owner, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Helpful for debugging
            return false;
        }
    }

    /**
     * Iterates through raw ingredient lines, finds suggestions, and prompts the user for clarification.
     *
     * @param rawIngredients The raw text from the ingredients text area.
     * @param owner          The parent frame for the dialog.
     * @return A string of verified ingredients, or null if the process is cancelled.
     * @throws Exception if an ingredient cannot be resolved.
     */
    private String resolveIngredients(String rawIngredients, JFrame owner) throws Exception {
        String[] lines = rawIngredients.split("\\n");
        StringBuilder verifiedIngredientsBuilder = new StringBuilder();

        JFrame parentFrame = (owner instanceof JFrame) ? (JFrame) owner : null;

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            Matcher matcher = ingredientPattern.matcher(line.trim());
            if (!matcher.matches()) {
                throw new Exception("Could not parse ingredient: '" + line + "'.\nPlease use format like '100g description'.");
            }

            String quantityStr = matcher.group(1);
            String description = matcher.group(2).trim();

            List<FoodItem> suggestions = dbManager.findFoodSuggestions(description, 20);

            if (suggestions.isEmpty()) {
                throw new Exception("No database match found for: '" + description + "'.");
            }

            FoodItem selected;
            if (suggestions.size() == 1) {
                selected = suggestions.get(0); // Auto-select if only one match
            } else {
                IngredientResolverDialog dialog = new IngredientResolverDialog(parentFrame, description, suggestions);
                selected = dialog.showDialog();
            }

            if (selected == null) {
                return null;
            }

            verifiedIngredientsBuilder.append(quantityStr).append("g ").append(selected.getDescription()).append("\n");
        }

        return verifiedIngredientsBuilder.toString();
    }

    public boolean deleteMeal(int mealId, JFrame owner) {
        if (dbManager.deleteMeal(mealId)) {
            JOptionPane.showMessageDialog(owner, "Meal deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            MealDataNotifier.getInstance().notifyMealDataChanged();
            return true;
        } else {
            JOptionPane.showMessageDialog(owner, "Failed to delete meal.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}