package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.NutrientCalculator;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VisualizationController {

    private final DBManager dbManager;
    private final NutrientCalculator nutrientCalculator;
    private final UserProfile userProfile;
    private final Pattern ingredientPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*g\\s*(.+)", Pattern.CASE_INSENSITIVE);


    // RDA values mimicking the RDAService, using official DB names.
    private static final Map<String, Double> RDA_VALUES = new LinkedHashMap<>();
    static {
        RDA_VALUES.put("ENERGY (KILOCALORIES)", 2000.0);
        RDA_VALUES.put("PROTEIN", 50.0);
        RDA_VALUES.put("FIBRE, TOTAL DIETARY", 30.0);
    }

    public VisualizationController(UserProfile userProfile) {
        this.dbManager = DBManager.getInstance();
        this.nutrientCalculator = new NutrientCalculator();
        this.userProfile = userProfile;
    }

    public DefaultPieDataset createMacroNutrientDataset(Date startDate, Date endDate) {
        Map<String, Double> avgDailyNutrients = getAverageDailyNutrients(startDate, endDate);
        DefaultPieDataset dataset = new DefaultPieDataset();

        double protein = 0;
        double carbs = 0;
        double fat = 0;

        for (Map.Entry<String, Double> entry : avgDailyNutrients.entrySet()) {
            if (entry.getKey().startsWith("PROTEIN")) {
                protein = entry.getValue();
            } else if (entry.getKey().startsWith("CARBOHYDRATE, TOTAL")) {
                carbs = entry.getValue();
            } else if (entry.getKey().startsWith("FAT (TOTAL LIPIDS)")) {
                fat = entry.getValue();
            }
        }

        dataset.setValue("Protein (g)", protein);
        dataset.setValue("Carbohydrates (g)", carbs);
        dataset.setValue("Fat (g)", fat);

        return dataset;
    }

    public DefaultPieDataset createMicroNutrientDataset(Date startDate, Date endDate) {
        Map<String, Double> avgDailyNutrients = getAverageDailyNutrients(startDate, endDate);
        if (avgDailyNutrients.isEmpty()) {
            return new DefaultPieDataset();
        }

        // Filter out macros and redundant energy units
        Map<String, Double> microNutrients = avgDailyNutrients.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("PROTEIN") &&
                        !entry.getKey().startsWith("CARBOHYDRATE, TOTAL") &&
                        !entry.getKey().startsWith("FAT (TOTAL LIPIDS)") &&
                        !entry.getKey().startsWith("ENERGY (KILOJOULES)") &&
                        !entry.getKey().startsWith("ENERGY (KILOCALORIES)") &&
                        !entry.getKey().startsWith("MOISTURE") &&
                        !entry.getKey().startsWith("FIBRE, TOTAL DIETARY") &&
                        entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Get top 5
        Map<String, Double> topNutrients = microNutrients.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // Sum the rest into "Other"
        double otherSum = microNutrients.entrySet().stream()
                .filter(entry -> !topNutrients.containsKey(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        DefaultPieDataset dataset = new DefaultPieDataset();
        topNutrients.forEach(dataset::setValue);
        if (otherSum > 0.01) {
            dataset.setValue("Other Nutrients", otherSum);
        }

        return dataset;
    }

    public String getRdaComparisonMessage(Date startDate, Date endDate) {
        Map<String, Double> avgDailyNutrients = getAverageDailyNutrients(startDate, endDate);
        if (avgDailyNutrients.isEmpty()) return "No data available for the selected period.";

        StringBuilder message = new StringBuilder("<html><b>Recommended Daily Allowance (RDA) Comparison:</b><br>");
        for (Map.Entry<String, Double> rda : RDA_VALUES.entrySet()) {
            String rdaNutrientName = rda.getKey();
            double recommended = rda.getValue();

            // Find the actual consumed value by matching the start of the string
            double actual = 0.0;
            for (Map.Entry<String, Double> avgEntry : avgDailyNutrients.entrySet()) {
                if (avgEntry.getKey().startsWith(rdaNutrientName)) {
                    actual = avgEntry.getValue();
                    break;
                }
            }

            double percentage = (recommended > 0) ? (actual / recommended) * 100 : 0;
            String displayName = rdaNutrientName.equals("ENERGY (KILOCALORIES)") ? "Calories" :
                    rdaNutrientName.equals("FIBRE, TOTAL DIETARY") ? "Fiber" : "Protein";

            message.append(String.format("- %s: You consumed %.1f%% of the recommended amount.<br>", displayName, percentage));
        }
        message.append("</html>");
        return message.toString();
    }

    /**
     * Helper method to find a nutrient value from a map using a partial, case-insensitive name.
     * @param nutrients The map of nutrient data (e.g., "PROTEIN (G)" -> 25.0).
     * @param nutrientName The simplified name to search for (e.g., "Protein").
     * @return The nutrient value, or 0.0 if not found.
     */
    private double getNutrientValue(Map<String, Double> nutrients, String nutrientName) {
        String searchName = nutrientName.toUpperCase();

        // Handle specific naming mismatches between UI and database
        if (searchName.equals("CALORIES")) {
            searchName = "ENERGY (KILOCALORIES)";
        } else if (searchName.equals("FIBER")) {
            searchName = "FIBRE"; // Correct the spelling for the database lookup
        }

        for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
            // Match the start of the key, ignoring case
            if (entry.getKey().toUpperCase().startsWith(searchName)) {
                return entry.getValue();
            }
        }
        return 0.0;
    }

    public DefaultCategoryDataset createSwapEffectDataset(Date startDate, Date endDate, String nutrient) {
        System.out.println("\n[DEBUG] VisController: --- Creating Swap Effect Dataset ---");
        System.out.println("[DEBUG] VisController: Date range: " + startDate + " to " + endDate);
        // Use the DB method to get ALL meals, including original ones that were swapped
        List<Meal> allMealsInRange = dbManager.getMealsForUser(userProfile.getId(), getStartOfDay(startDate), getEndOfDay(endDate), true);
        System.out.println("[DEBUG] VisController: Fetched " + allMealsInRange.size() + " total meals (including replaced ones).");
        if (allMealsInRange.isEmpty()) return new DefaultCategoryDataset();

        Map<Integer, Meal> originalMealsById = new HashMap<>();
        Map<Integer, Meal> swappedMealsByOriginalId = new HashMap<>();

        for (Meal meal : allMealsInRange) {
            if (meal.isSwapped() && meal.getOriginalMealId() != null) {
                swappedMealsByOriginalId.put(meal.getOriginalMealId(), meal);
            } else if (!meal.isSwapped()) {
                originalMealsById.put(meal.getMealId(), meal);
            }
        }
        System.out.println("[DEBUG] VisController: Found " + originalMealsById.size() + " original meals and " + swappedMealsByOriginalId.size() + " swapped meals.");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, List<Meal>> originalMealsGroupedByDate = originalMealsById.values().stream()
                .collect(Collectors.groupingBy(m -> sdf.format(m.getDate())));

        originalMealsGroupedByDate.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String date = entry.getKey();
            List<Meal> dailyOriginalMeals = entry.getValue();
            double beforeValue = 0;
            double afterValue = 0;
            boolean wasSwappedOnThisDay = false; // Flag to track if any swap occurred on this day

            // Calculate the total "before" and "after" values for the entire day
            for (Meal originalMeal : dailyOriginalMeals) {
                Map<String, Double> beforeNutrients = nutrientCalculator.calculateNutrientsForMeal(originalMeal.getIngredients());
                beforeValue += getNutrientValue(beforeNutrients, nutrient);

                // Check if this original meal has a corresponding swapped meal
                if (swappedMealsByOriginalId.containsKey(originalMeal.getMealId())) {
                    wasSwappedOnThisDay = true; // Mark that a swap happened today
                    Meal swappedMeal = swappedMealsByOriginalId.get(originalMeal.getMealId());
                    Map<String, Double> afterNutrients = nutrientCalculator.calculateNutrientsForMeal(swappedMeal.getIngredients());
                    afterValue += getNutrientValue(afterNutrients, nutrient);
                } else {
                    // If no swap exists for this meal, the "after" value is the same as the "before"
                    afterValue += getNutrientValue(beforeNutrients, nutrient);
                }
            }

            // Only add data to the chart if there was at least one swap on that day
            if (wasSwappedOnThisDay) {
                System.out.println("[DEBUG] VisController: Adding to swap dataset for date " + date + ": Before=" + beforeValue + ", After=" + afterValue);
                dataset.addValue(beforeValue, "Before Swaps", date);
                dataset.addValue(afterValue, "After Swaps", date);
            }
        });

        System.out.println("[DEBUG] VisController: Final Swap Dataset has " + dataset.getRowCount() + " rows and " + dataset.getColumnCount() + " columns.");
        System.out.println("[DEBUG] VisController: --- Finished Swap Effect Dataset ---\n");
        return dataset;
    }


    /**
     * Analyzes meal data over a period to determine the user's food group distribution.
     * @param startDate The start of the analysis period.
     * @param endDate The end of the analysis period.
     * @return A DefaultPieDataset showing the percentage of intake from each food group.
     */
    public DefaultPieDataset createCfgComparisonDataset(Date startDate, Date endDate) {
        System.out.println("\n[DEBUG] VisController: --- Creating CFG Comparison Dataset ---");
        List<Meal> meals = dbManager.getMealsForUser(userProfile.getId(), getStartOfDay(startDate), getEndOfDay(endDate));
        if (meals.isEmpty()) {
            System.out.println("[DEBUG] VisController: No active meals found for CFG analysis.");
            return new DefaultPieDataset();
        }


        Map<String, Double> foodGroupWeights = new HashMap<>();
        for (Meal meal : meals) {
            String[] ingredients = meal.getIngredients().split("\n");
            for (String ingredient : ingredients) {
                if (!ingredient.trim().isEmpty()) {
                    // Use regex to parse the weight from the ingredient string.
                    Matcher matcher = ingredientPattern.matcher(ingredient.trim());
                    double weight = 0.0;
                    if (matcher.matches()) {
                        try {
                            weight = Double.parseDouble(matcher.group(1));
                        } catch (NumberFormatException e) {
                            System.err.println("Could not parse weight from: " + ingredient);
                        }
                    }

                    String foodGroup = dbManager.getFoodGroup(ingredient.trim());
                    if (foodGroup != null) {
                        System.out.println("[DEBUG] Ingredient: '" + ingredient.trim() + "' -> DB Food Group: '" + foodGroup + "'");
                        foodGroup = normalizeFoodGroup(foodGroup);
                        System.out.println("    -> Normalized Group: '" + foodGroup + "' with weight: " + weight);

                        foodGroupWeights.merge(foodGroup, weight, Double::sum);
                    } else {
                        System.out.println("[DEBUG] Ingredient: '" + ingredient.trim() + "' -> DB Food Group: NOT FOUND");

                        foodGroupWeights.merge("Uncategorized", weight, Double::sum);
                    }
                }
            }
        }
        System.out.println("[DEBUG] VisController: Final Food Group Weights: " + foodGroupWeights);

        DefaultPieDataset dataset = new DefaultPieDataset();

        foodGroupWeights.forEach((group, totalWeight) -> {
            if (totalWeight > 0) {
                dataset.setValue(group, totalWeight);
            }
        });

        System.out.println("[DEBUG] VisController: --- Finished CFG Comparison Dataset ---\n");
        return dataset;
    }

    /**
     * Normalizes different food group names from the database into the main categories
     * used by Canada's Food Guide.
     * @param dbFoodGroup The food group name from the database.
     * @return The normalized food group name.
     */
    private String normalizeFoodGroup(String dbFoodGroup) {
        String lowerCaseGroup = dbFoodGroup.toLowerCase();
        if (lowerCaseGroup.contains("vegetable") || lowerCaseGroup.contains("fruit")) {
            return "Vegetables and Fruit";
        } else if (lowerCaseGroup.contains("grain") || lowerCaseGroup.contains("cereal") || lowerCaseGroup.contains("baked")) {
            return "Grain Products";
        } else if (lowerCaseGroup.contains("dairy") || lowerCaseGroup.contains("milk")) {
            return "Milk and Alternatives";
        } else if (lowerCaseGroup.contains("meat") || lowerCaseGroup.contains("poultry") || lowerCaseGroup.contains("legumes") ||
                lowerCaseGroup.contains("nut") || lowerCaseGroup.contains("pork") || lowerCaseGroup.contains("beef") ||
                lowerCaseGroup.contains("finfish") || lowerCaseGroup.contains("shellfish") || lowerCaseGroup.contains("sausage")) {
            return "Meat and Alternatives";
        }
        return "Other";
    }

    private Map<String, Double> getAverageDailyNutrients(Date startDate, Date endDate) {
        // excludes replaced meals for this calculation
        System.out.println("[DEBUG] VisController: Calculating average nutrients for date range: " + startDate + " to " + endDate);
        List<Meal> meals = dbManager.getMealsForUser(userProfile.getId(), getStartOfDay(startDate), getEndOfDay(endDate));
        if (meals.isEmpty()) {
            System.out.println("[DEBUG] VisController: No active meals found to calculate averages.");
            return new HashMap<>();
        }
        System.out.println("[DEBUG] VisController: Found " + meals.size() + " active meals for averaging.");

        Map<String, Double> totalNutrients = new HashMap<>();
        for (Meal meal : meals) {
            Map<String, Double> mealNutrients = new NutrientCalculator().calculateNutrientsForMeal(meal.getIngredients());
            mealNutrients.forEach((key, value) -> totalNutrients.merge(key, value, Double::sum));
        }

        long diffInMillis = Math.abs(getEndOfDay(endDate).getTime() - getStartOfDay(startDate).getTime());
        long days = TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1;
        System.out.println("[DEBUG] VisController: Number of days in range: " + days);

        Map<String, Double> avgDailyNutrients = new HashMap<>();
        for (Map.Entry<String, Double> entry : totalNutrients.entrySet()) {
            avgDailyNutrients.put(entry.getKey(), entry.getValue() / days);
        }
        return avgDailyNutrients;
    }

    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }
}