package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.NutrientCalculator;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
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

    // Replace conditional with Map for food group normalization
    private static final Map<String, String> FOOD_GROUP_MAPPING = new HashMap<>();
    static {
        FOOD_GROUP_MAPPING.put("vegetable", "Vegetables and Fruit");
        FOOD_GROUP_MAPPING.put("fruit", "Vegetables and Fruit");
        FOOD_GROUP_MAPPING.put("grain", "Grain Products");
        FOOD_GROUP_MAPPING.put("cereal", "Grain Products");
        FOOD_GROUP_MAPPING.put("baked", "Grain Products");
        FOOD_GROUP_MAPPING.put("dairy", "Milk and Alternatives");
        FOOD_GROUP_MAPPING.put("milk", "Milk and Alternatives");
        FOOD_GROUP_MAPPING.put("meat", "Meat and Alternatives");
        FOOD_GROUP_MAPPING.put("poultry", "Meat and Alternatives");
        FOOD_GROUP_MAPPING.put("legumes", "Meat and Alternatives");
        FOOD_GROUP_MAPPING.put("nut", "Meat and Alternatives");
        FOOD_GROUP_MAPPING.put("pork", "Meat and Alternatives");
        FOOD_GROUP_MAPPING.put("beef", "Meat and Alternatives");
        FOOD_GROUP_MAPPING.put("finfish", "Meat and Alternatives");
        FOOD_GROUP_MAPPING.put("shellfish", "Meat and Alternatives");
        FOOD_GROUP_MAPPING.put("sausage", "Meat and Alternatives");
    }

    private static final Map<String, Map<String, Map<String, Double>>> RDA_VALUES = new HashMap<>();

    static {
        // Initialize RDA values based on the provided table
        // Males
        Map<String, Map<String, Double>> maleValues = new HashMap<>();
        maleValues.put("9-13", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 34.0);
            put("Total Fibre", 31.0);
        }});
        maleValues.put("14-18", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 52.0);
            put("Total Fibre", 38.0);
        }});
        maleValues.put("19-30", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 56.0);
            put("Total Fibre", 38.0);
        }});
        maleValues.put("31-50", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 56.0);
            put("Total Fibre", 38.0);
        }});
        maleValues.put("51-70", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 56.0);
            put("Total Fibre", 30.0);
        }});
        maleValues.put(">70", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 56.0);
            put("Total Fibre", 30.0);
        }});
        RDA_VALUES.put("Male", maleValues);

        // Females
        Map<String, Map<String, Double>> femaleValues = new HashMap<>();
        femaleValues.put("9-13", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 34.0);
            put("Total Fibre", 26.0);
        }});
        femaleValues.put("14-18", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 46.0);
            put("Total Fibre", 26.0);
        }});
        femaleValues.put("19-30", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 46.0);
            put("Total Fibre", 25.0);
        }});
        femaleValues.put("31-50", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 46.0);
            put("Total Fibre", 25.0);
        }});
        femaleValues.put("51-70", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 46.0);
            put("Total Fibre", 21.0);
        }});
        femaleValues.put(">70", new HashMap<>() {{
            put("Carbohydrate (Digestible)", 130.0);
            put("Total Protein", 46.0);
            put("Total Fibre", 21.0);
        }});
        RDA_VALUES.put("Female", femaleValues);
    }

    /**
     * Modified constructor to accept dependencies for better testability.
     */
    public VisualizationController(UserProfile userProfile, DBManager dbManager, NutrientCalculator nutrientCalculator) {
        this.userProfile = userProfile;
        this.dbManager = dbManager;
        this.nutrientCalculator = nutrientCalculator;
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
        long diffInMillis = Math.abs(getEndOfDay(endDate).getTime() - getStartOfDay(startDate).getTime());
        long days = TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1;

        Map<String, Double> totalConsumedNutrients = getTotalNutrientsInPeriod(startDate, endDate);
        if (totalConsumedNutrients.isEmpty()) return "No data available for the selected period.";

        Map<String, Double> rdaForUser = getRdaForUser();

        String periodString = "for the selected period of " + days + " day(s)";
        if (days == 1) {
            periodString = "for the selected day";
        }

        StringBuilder message = new StringBuilder("<html><b>Recommended Daily Allowance (RDA) Comparison " + periodString + ":</b><br>");

        // A map to link RDA names to DB names and display names, ensuring correct order.
        Map<String, String[]> nutrientMapping = new LinkedHashMap<>();
        nutrientMapping.put("Total Protein", new String[]{"PROTEIN", "Protein"});
        nutrientMapping.put("Carbohydrate (Digestible)", new String[]{"CARBOHYDRATE", "Carbohydrate"});
        nutrientMapping.put("Total Fibre", new String[]{"FIBRE", "Fibre"});


        for (Map.Entry<String, String[]> mappingEntry : nutrientMapping.entrySet()) {
            String rdaKey = mappingEntry.getKey();
            String dbSearchKey = mappingEntry.getValue()[0];
            String displayName = mappingEntry.getValue()[1];

            if (rdaForUser.containsKey(rdaKey)) {
                double dailyRecommended = rdaForUser.get(rdaKey);
                double totalRecommended = dailyRecommended * days;
                double actualTotal = 0.0;

                // Find the actual consumed value from the total map
                for (Map.Entry<String, Double> consumedEntry : totalConsumedNutrients.entrySet()) {
                    if (consumedEntry.getKey().toUpperCase().startsWith(dbSearchKey)) {
                        actualTotal = consumedEntry.getValue();
                        break;
                    }
                }

                double percentage = (totalRecommended > 0) ? (actualTotal / totalRecommended) * 100 : 0;

                message.append(String.format("- %s: You consumed %.1f%% of the recommended amount.<br>", displayName, percentage));
            }
        }

        message.append("</html>");
        return message.toString();
    }

    private Map<String, Double> getTotalNutrientsInPeriod(Date startDate, Date endDate) {
        List<Meal> meals = dbManager.getMealsForUser(userProfile.getId(), getStartOfDay(startDate), getEndOfDay(endDate));
        if (meals.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Double> totalNutrients = new HashMap<>();
        for (Meal meal : meals) {
            Map<String, Double> mealNutrients = nutrientCalculator.calculateNutrientsForMeal(meal.getIngredients());
            mealNutrients.forEach((key, value) -> totalNutrients.merge(key, value, Double::sum));
        }
        return totalNutrients;
    }


    private Map<String, Double> getRdaForUser() {
        LocalDate birthDate = new java.sql.Date(userProfile.getDateOfBirth().getTime()).toLocalDate();
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        String sex = userProfile.getSex();
        String ageRange = getAgeRange(age);

        return RDA_VALUES.getOrDefault(sex, new HashMap<>()).getOrDefault(ageRange, new HashMap<>());
    }

    private String getAgeRange(int age) {
        if (age >= 9 && age <= 13) return "9-13";
        if (age >= 14 && age <= 18) return "14-18";
        if (age >= 19 && age <= 30) return "19-30";
        if (age >= 31 && age <= 50) return "31-50";
        if (age >= 51 && age <= 70) return "51-70";
        if (age > 70) return ">70";
        return ""; // Default case
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
     * Updated to use the new dbManager method that handles the logic.
     */
    public DefaultPieDataset createCfgComparisonDataset(Date startDate, Date endDate) {
        System.out.println("\n[DEBUG] VisController: --- Creating CFG Comparison Dataset ---");
        
        // Use the new method from dbManager that handles the complex logic
        Map<String, Double> foodGroupWeights = dbManager.getFoodGroupDistribution(userProfile.getId(), 
                getStartOfDay(startDate), getEndOfDay(endDate));
        
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
     * Refactored to use Map instead of complex conditional logic.
     */
    private String normalizeFoodGroup(String dbFoodGroup) {
        String lowerCaseGroup = dbFoodGroup.toLowerCase();
        
        // Use the map to find the appropriate category
        for (Map.Entry<String, String> entry : FOOD_GROUP_MAPPING.entrySet()) {
            if (lowerCaseGroup.contains(entry.getKey())) {
                return entry.getValue();
            }
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
            Map<String, Double> mealNutrients = nutrientCalculator.calculateNutrientsForMeal(meal.getIngredients());
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