package com.nutri_sci.service;

import com.nutri_sci.database.DBManager;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NutrientCalculator {

    private final DBManager dbManager;
    private final Pattern ingredientPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*g\\s*(.+)", Pattern.CASE_INSENSITIVE);

    public NutrientCalculator() {
        this.dbManager = DBManager.getInstance();
    }

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