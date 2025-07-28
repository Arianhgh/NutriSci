package com.nutri_sci.service;

import com.nutri_sci.database.DBManager;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service class responsible for calculating nutritional values for meals.
 * <p>
 * The NutrientCalculator parses ingredient strings and computes comprehensive
 * nutritional information by looking up individual ingredients in the nutrition
 * database and aggregating their values based on specified quantities.
 * </p>
 * <p>
 * This class expects ingredients to be formatted as "quantity unit description"
 * (e.g., "100g chicken breast", "250g brown rice"). It uses regular expressions
 * to parse this format and applies proportional scaling based on the specified
 * quantities.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.database.DBManager
 */
public class NutrientCalculator {

    /** Database manager for accessing nutritional information */
    private final DBManager dbManager;
    
    /** Regular expression pattern for parsing ingredient lines in format "100g chicken breast" */
    private final Pattern ingredientPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*g\\s*(.+)", Pattern.CASE_INSENSITIVE);

    /**
     * Constructs a new NutrientCalculator with the specified database manager.
     * 
     * @param dbManager the database manager for accessing nutritional data
     */
    public NutrientCalculator(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Calculates the total nutritional values for a meal based on its ingredients.
     * <p>
     * This method parses a multi-line ingredient string where each line represents
     * a single ingredient in the format "100g chicken breast". It looks up each
     * ingredient in the nutrition database, scales the nutritional values according
     * to the specified quantity, and aggregates all values to produce a comprehensive
     * nutritional profile for the entire meal.
     * </p>
     * <p>
     * The method handles parsing errors gracefully by logging warnings and continuing
     * with other ingredients. If an ingredient cannot be found in the database or
     * parsed correctly, it is skipped without affecting the calculation of other
     * ingredients.
     * </p>
     * 
     * @param ingredients a multi-line string where each line represents an ingredient
     *                   in the format "quantityg description" (e.g., "100g chicken breast\n200g rice")
     * @return a map containing nutrient names as keys and total amounts as values
     *         (e.g., "Protein" -> 45.5, "Calories" -> 450.0)
     */
    public Map<String, Double> calculateNutrientsForMeal(String ingredients) {
        Map<String, Double> totalNutrients = new HashMap<>();
        if (ingredients == null || ingredients.trim().isEmpty()) {
            return totalNutrients;
        }

        String[] ingredientLines = ingredients.split("\\n");

        for (String line : ingredientLines) {
            if (line.trim().isEmpty()) {
                continue;
            }

            Matcher matcher = ingredientPattern.matcher(line.trim());

            if (matcher.matches()) {
                try {
                    double grams = Double.parseDouble(matcher.group(1));
                    String description = matcher.group(2).trim();

                    // Use the comprehensive profile for detailed view
                    Map<String, Double> nutrientsPer100g = dbManager.getComprehensiveNutrientProfile(description);

                    for (Map.Entry<String, Double> entry : nutrientsPer100g.entrySet()) {
                        double ingredientNutrientValue = (entry.getValue() / 100.0) * grams;
                        totalNutrients.merge(entry.getKey(), ingredientNutrientValue, Double::sum);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse weight from line: " + line);
                }
            } else {
                System.err.println("Could not parse ingredient line: '" + line + "'. Expected format: '[amount]g [description]'");
            }
        }
        return totalNutrients;
    }
}