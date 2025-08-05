package com.nutri_sci.database;

import com.nutri_sci.model.FoodItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Repository for managing food and nutrition data.
 * Extracted from DBManager to follow Single Responsibility Principle.
 */
public class FoodRepository {
    private final Connection connection;
    
    /** Nutrient ID for calories in the NUTRIENT_NAME table */
    private static final int CALORIE_NUTRIENT_ID = 208;
    
    /** Nutrient ID for protein in the NUTRIENT_NAME table */
    private static final int PROTEIN_NUTRIENT_ID = 203;
    
    /** Nutrient ID for fiber in the NUTRIENT_NAME table */
    private static final int FIBER_NUTRIENT_ID = 291;

    /** Regular expression pattern for parsing ingredient lines (e.g., "100g chicken breast") */
    private final Pattern ingredientPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*g\\s*(.+)", Pattern.CASE_INSENSITIVE);

    public FoodRepository(Connection connection) {
        this.connection = connection;
    }

    public List<String> getFoodsByNutrientRank(String nutrientName, String rank) {
        int nutrientId;
        switch (nutrientName) {
            case "Protein":
                nutrientId = PROTEIN_NUTRIENT_ID;
                break;
            case "Fiber":
                nutrientId = FIBER_NUTRIENT_ID;
                break;
            case "Calories":
            default:
                nutrientId = CALORIE_NUTRIENT_ID;
                break;
        }

        List<String> foods = new ArrayList<>();
        String sortOrder = rank.equalsIgnoreCase("HIGH") ? "DESC" : "ASC";
        String sql = "SELECT FN.FoodDescription FROM NUTRIENT_AMOUNT NA " +
                "JOIN FOOD_NAME FN ON NA.FoodID = FN.FoodID " +
                "WHERE NA.NutrientID = ? " +
                "ORDER BY NA.NutrientValue " + sortOrder + " " +
                "LIMIT 300";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, nutrientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                foods.add(rs.getString("FoodDescription"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foods;
    }

    public double getCaloriesPer100g(int foodId) {
        String sql = "SELECT NutrientValue FROM NUTRIENT_AMOUNT WHERE FoodID = ? AND NutrientID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, foodId);
            pstmt.setInt(2, CALORIE_NUTRIENT_ID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("NutrientValue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Builds the SQL query for food suggestions.
     * Extracted method to reduce complexity in findFoodSuggestions.
     */
    private String buildFoodSuggestionQuery(String[] words) {
        int wordsToUse = Math.min(words.length, 5);
        StringBuilder scoreBuilder = new StringBuilder();
        StringBuilder whereBuilder = new StringBuilder();

        for (int i = 0; i < wordsToUse; i++) {
            if (i > 0) {
                scoreBuilder.append(" + ");
                whereBuilder.append(" OR ");
            }
            scoreBuilder.append("(FoodDescription REGEXP ?)");
            whereBuilder.append("FoodDescription REGEXP ?");
        }

        return "SELECT FoodID, FoodDescription, (" + scoreBuilder.toString() + ") AS match_score " +
                "FROM FOOD_NAME " +
                "WHERE " + whereBuilder.toString() + " " +
                "ORDER BY " +
                "match_score DESC, " +
                "CASE " +
                "    WHEN FoodDescription LIKE ? THEN 0 " +
                "    WHEN FoodDescription NOT LIKE '%cooked%' AND FoodDescription NOT LIKE '%canned%' AND FoodDescription NOT LIKE '%frozen%' AND FoodDescription NOT LIKE '%sauce%' AND FoodDescription NOT LIKE '%soup%' AND FoodDescription NOT LIKE '%dish%' THEN 1 " +
                "    ELSE 2 " +
                "END ASC, " +
                "LENGTH(FoodDescription) ASC " +
                "LIMIT ?";
    }

    public List<FoodItem> findFoodSuggestions(String description, int limit) {
        List<FoodItem> suggestions = new ArrayList<>();
        String sanitizedDescription = description.trim().replace(",", "");
        String[] words = sanitizedDescription.split("\\s+");
        if (words.length == 0 || sanitizedDescription.isEmpty()) {
            return suggestions;
        }

        int wordsToUse = Math.min(words.length, 5);
        List<String> regexPatterns = new ArrayList<>();

        for (int i = 0; i < wordsToUse; i++) {
            String word = words[i].toLowerCase();
            String pattern;

            String sanitizedWord = word.replaceAll("([\\\\\\.\\[\\]\\{\\}\\(\\)\\*\\+\\?\\^\\$\\|])", "\\\\$1");

            if (sanitizedWord.endsWith("es")) {
                String base = sanitizedWord.substring(0, sanitizedWord.length() - 2);
                pattern = "\\b" + base + "(es)?\\b";
            } else if (sanitizedWord.endsWith("s") && !sanitizedWord.endsWith("ss")) {
                String base = sanitizedWord.substring(0, sanitizedWord.length() - 1);
                pattern = "\\b" + base + "(s)?\\b";
            } else {
                pattern = "\\b" + sanitizedWord + "\\b";
            }
            regexPatterns.add(pattern);
        }

        String sql = buildFoodSuggestionQuery(words);

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;

            for (String p : regexPatterns) {
                pstmt.setString(paramIndex++, p);
            }

            for (String p : regexPatterns) {
                pstmt.setString(paramIndex++, p);
            }

            pstmt.setString(paramIndex++, "%" + description + "%");
            pstmt.setInt(paramIndex, limit);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                suggestions.add(new FoodItem(
                        rs.getInt("FoodID"),
                        rs.getString("FoodDescription")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suggestions;
    }

    public int findFoodIdByExactDescription(String description) {
        String sql = "SELECT FoodID FROM FOOD_NAME WHERE FoodDescription = ? LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, description);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("FoodID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getFoodGroup(String fullIngredientLine) {
        Matcher matcher = ingredientPattern.matcher(fullIngredientLine.trim());
        if (!matcher.matches()) {
            return null;
        }
        String description = matcher.group(2).trim();
        int foodId = findFoodIdByExactDescription(description);

        if (foodId == -1) return null;

        String sql = "SELECT FG.FoodGroupName FROM FOOD_NAME FN JOIN FOOD_GROUP FG ON FN.FoodGroupID = FG.FoodGroupID WHERE FN.FoodID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, foodId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("FoodGroupName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getFoodsFromGroup(String foodGroup) {
        List<String> foods = new ArrayList<>();
        String sql = "SELECT FN.FoodDescription FROM FOOD_NAME FN JOIN FOOD_GROUP FG ON FN.FoodGroupID = FG.FoodGroupID WHERE FG.FoodGroupName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, foodGroup);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                foods.add(rs.getString("FoodDescription"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foods;
    }

    public Map<String, Double> getNutrientProfile(String foodDescription) {
        int foodId = findFoodIdByExactDescription(foodDescription);
        if (foodId == -1) return new HashMap<>();
        return getNutrientProfileById(foodId);
    }

    private Map<String, Double> getNutrientProfileById(int foodId) {
        Map<String, Double> nutrients = new HashMap<>();
        String sql = "SELECT NutrientID, NutrientValue FROM NUTRIENT_AMOUNT WHERE FoodID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, foodId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int nutrientId = rs.getInt("NutrientID");
                double value = rs.getDouble("NutrientValue");
                if (nutrientId == CALORIE_NUTRIENT_ID) nutrients.put("Calories", value);
                else if (nutrientId == PROTEIN_NUTRIENT_ID) nutrients.put("Protein", value);
                else if (nutrientId == FIBER_NUTRIENT_ID) nutrients.put("Fiber", value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nutrients;
    }

    public Map<String, Double> getComprehensiveNutrientProfile(String foodDescription) {
        int foodId = findFoodIdByExactDescription(foodDescription);
        if (foodId == -1) return new HashMap<>();

        Map<String, Double> nutrients = new HashMap<>();
        String sql = "SELECT na.NutrientValue, nn.NutrientName, nn.NutrientUnit " +
                "FROM NUTRIENT_AMOUNT na " +
                "JOIN NUTRIENT_NAME nn ON na.NutrientID = nn.NutrientID " +
                "WHERE na.FoodID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, foodId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("NutrientName");
                double value = rs.getDouble("NutrientValue");
                String unit = rs.getString("NutrientUnit");
                nutrients.put(name + " (" + unit + ")", value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nutrients;
    }

    /**
     * Gets food group distribution for a user over a date range.
     * Moved from VisualizationController to improve separation of concerns.
     */
    public Map<String, Double> getFoodGroupDistribution(int userId, java.util.Date startDate, java.util.Date endDate) {
        String sql = "SELECT ml.Ingredients FROM MEAL_LOG ml WHERE ml.UserID = ? AND ml.MealDate >= ? AND ml.MealDate <= ? " +
                "AND ml.MealID NOT IN (SELECT OriginalMealID FROM MEAL_LOG WHERE OriginalMealID IS NOT NULL AND UserID = ?)";
        
        Map<String, Double> foodGroupWeights = new HashMap<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setTimestamp(2, new Timestamp(startDate.getTime()));
            pstmt.setTimestamp(3, new Timestamp(endDate.getTime()));
            pstmt.setInt(4, userId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String ingredients = rs.getString("Ingredients");
                String[] ingredientLines = ingredients.split("\n");
                
                for (String ingredient : ingredientLines) {
                    if (!ingredient.trim().isEmpty()) {
                        Matcher matcher = ingredientPattern.matcher(ingredient.trim());
                        double weight = 0.0;
                        if (matcher.matches()) {
                            try {
                                weight = Double.parseDouble(matcher.group(1));
                            } catch (NumberFormatException e) {
                                System.err.println("Could not parse weight from: " + ingredient);
                            }
                        }

                        String foodGroup = getFoodGroup(ingredient.trim());
                        if (foodGroup != null) {
                            foodGroup = normalizeFoodGroup(foodGroup);
                            foodGroupWeights.merge(foodGroup, weight, Double::sum);
                        } else {
                            foodGroupWeights.merge("Uncategorized", weight, Double::sum);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return foodGroupWeights;
    }

    /**
     * Normalizes different food group names from the database into the main categories
     * used by Canada's Food Guide.
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
} 