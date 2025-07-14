package com.nutri_sci.database;

import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;

import java.sql.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages all database connections and queries for the application.
 * Implements the Singleton pattern to ensure only one instance exists.
 */
public class DBManager {
    // The single, static instance of the DBManager for the Singleton pattern.
    private static DBManager instance;
    private Connection connection;

    // Database connection details.
    private static final String DB_URL = "jdbc:mysql://localhost/nutrisci_db";
    private static final String USER = "root";
    private static final String PASS = "root";

    // Constant IDs mapping to the primary keys in the NUTRIENT_NAME table.
    private static final int CALORIE_NUTRIENT_ID = 208;
    private static final int PROTEIN_NUTRIENT_ID = 203;
    private static final int FIBER_NUTRIENT_ID = 291;

    // Regex to parse ingredient strings like "100g chicken breast".
    private final Pattern ingredientPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*g\\s*(.+)", Pattern.CASE_INSENSITIVE);

    /**
     * Private constructor to prevent direct instantiation (part of Singleton pattern).
     */
    private DBManager() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            createApplicationTables();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the database.");
        }
    }

    /**
     * Creates application-specific tables if they don't already exist.
     */
    private void createApplicationTables() {
        String createUserProfileTable = "CREATE TABLE IF NOT EXISTS USER_PROFILE ("
                + "UserID INT PRIMARY KEY AUTO_INCREMENT,"
                + "ProfileName VARCHAR(255) NOT NULL UNIQUE,"
                + "Sex VARCHAR(50),"
                + "DateOfBirth DATE,"
                + "HeightCM DOUBLE,"
                + "WeightKG DOUBLE,"
                + "MeasurementUnit VARCHAR(50)"
                + ");";

        String createMealLogTable = "CREATE TABLE IF NOT EXISTS MEAL_LOG ("
                + "MealID INT PRIMARY KEY AUTO_INCREMENT,"
                + "UserID INT NOT NULL,"
                + "MealDate DATETIME NOT NULL,"
                + "MealType VARCHAR(50) NOT NULL,"
                + "Ingredients TEXT,"
                + "EstimatedCalories DOUBLE,"
                + "IsSwapped BOOLEAN DEFAULT FALSE,"
                + "OriginalMealID INT NULL,"
                + "FOREIGN KEY (UserID) REFERENCES USER_PROFILE(UserID) ON DELETE CASCADE,"
                + "FOREIGN KEY (OriginalMealID) REFERENCES MEAL_LOG(MealID) ON DELETE SET NULL"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createUserProfileTable);
            stmt.executeUpdate(createMealLogTable);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create or verify application tables.", e);
        }
    }

    /**
     * Provides global access to the single DBManager instance, creating it if necessary.
     */
    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    public Date getMostRecentMealDate(int userId) {
        String sql = "SELECT MAX(MealDate) AS latestDate FROM MEAL_LOG WHERE UserID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("latestDate");
                if (ts != null) {
                    return new Date(ts.getTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no meals are found
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

    public UserProfile saveProfile(UserProfile profile) {
        String sql = "INSERT INTO USER_PROFILE (ProfileName, Sex, DateOfBirth, HeightCM, WeightKG, MeasurementUnit) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, profile.getName());
            pstmt.setString(2, profile.getSex());
            pstmt.setDate(3, new java.sql.Date(profile.getDateOfBirth().getTime()));
            pstmt.setDouble(4, profile.getHeight());
            pstmt.setDouble(5, profile.getWeight());
            pstmt.setString(6, profile.getMeasurementUnit());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                profile.setId(generatedKeys.getInt(1));
            }
            return profile;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public UserProfile getProfile(String profileName) {
        String sql = "SELECT * FROM USER_PROFILE WHERE ProfileName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, profileName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                UserProfile profile = new UserProfile();
                profile.setId(rs.getInt("UserID"));
                profile.setName(rs.getString("ProfileName"));
                profile.setSex(rs.getString("Sex"));
                profile.setDateOfBirth(rs.getDate("DateOfBirth"));
                profile.setHeight(rs.getDouble("HeightCM"));
                profile.setWeight(rs.getDouble("WeightKG"));
                profile.setMeasurementUnit(rs.getString("MeasurementUnit"));
                return profile;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<String> getAllUserNames() {
        Set<String> userNames = new HashSet<>();
        String sql = "SELECT ProfileName FROM USER_PROFILE";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                userNames.add(rs.getString("ProfileName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userNames;
    }

    public boolean saveMeal(int userId, Meal meal) {
        String sql = "INSERT INTO MEAL_LOG (UserID, MealDate, MealType, Ingredients, EstimatedCalories, IsSwapped, OriginalMealID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setTimestamp(2, new Timestamp(meal.getDate().getTime()));
            pstmt.setString(3, meal.getMealType());
            pstmt.setString(4, meal.getIngredients());
            pstmt.setDouble(5, meal.getEstimatedCalories());
            pstmt.setBoolean(6, meal.isSwapped());
            if (meal.getOriginalMealId() != null) {
                pstmt.setInt(7, meal.getOriginalMealId());
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Meal> getMealsForUser(int userId) {
        return getMealsForUser(userId, null, null, false);
    }

    public List<Meal> getMealsForUser(int userId, Date startDate, Date endDate) {
        return getMealsForUser(userId, startDate, endDate, false);
    }

    public List<Meal> getMealsForUser(int userId, Date startDate, Date endDate, boolean includeReplacedMeals) {
        List<Meal> meals = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM MEAL_LOG WHERE UserID = ?");

        if (!includeReplacedMeals) {
            sql.append(" AND MealID NOT IN (SELECT OriginalMealID FROM MEAL_LOG WHERE OriginalMealID IS NOT NULL AND UserID = ?)");
        }

        if (startDate != null) {
            sql.append(" AND MealDate >= ?");
        }
        if (endDate != null) {
            sql.append(" AND MealDate <= ?");
        }
        sql.append(" ORDER BY MealDate DESC");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            pstmt.setInt(paramIndex++, userId);
            if (!includeReplacedMeals) {
                pstmt.setInt(paramIndex++, userId);
            }
            if (startDate != null) {
                pstmt.setTimestamp(paramIndex++, new Timestamp(startDate.getTime()));
            }
            if (endDate != null) {
                pstmt.setTimestamp(paramIndex++, new Timestamp(endDate.getTime()));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Meal meal = new Meal();
                meal.setMealId(rs.getInt("MealID"));
                meal.setDate(rs.getTimestamp("MealDate"));
                meal.setMealType(rs.getString("MealType"));
                meal.setIngredients(rs.getString("Ingredients"));
                meal.setEstimatedCalories(rs.getDouble("EstimatedCalories"));
                meal.setSwapped(rs.getBoolean("IsSwapped"));
                meal.setOriginalMealId((Integer) rs.getObject("OriginalMealID"));
                meals.add(meal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meals;
    }


    /**
     * Retrieves a single meal by its unique ID.
     * @param mealId The ID of the meal to retrieve.
     * @return The Meal object, or null if not found.
     */
    public Meal getMealById(int mealId) {
        String sql = "SELECT * FROM MEAL_LOG WHERE MealID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, mealId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Meal meal = new Meal();
                meal.setMealId(rs.getInt("MealID"));
                meal.setDate(rs.getTimestamp("MealDate"));
                meal.setMealType(rs.getString("MealType"));
                meal.setIngredients(rs.getString("Ingredients"));
                meal.setEstimatedCalories(rs.getDouble("EstimatedCalories"));
                meal.setSwapped(rs.getBoolean("IsSwapped"));
                meal.setOriginalMealId((Integer) rs.getObject("OriginalMealID"));
                return meal;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean hasUserLoggedMealTypeOnDate(int userId, String mealType, java.util.Date date) {
        String sql = "SELECT COUNT(*) FROM MEAL_LOG WHERE UserID = ? AND MealType = ? AND DATE(MealDate) = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, mealType);
            pstmt.setDate(3, new java.sql.Date(date.getTime()));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

    public int findFoodId(String description) {
        String sanitizedDescription = description.trim().replace(",", "");
        String[] words = sanitizedDescription.split("\\s+");
        if (words.length == 0) return -1;

        // Use at most the first 4 words to avoid overly strict searches.
        int wordsToUse = Math.min(words.length, 4);

        StringBuilder sql = new StringBuilder("SELECT FoodID, FoodDescription FROM FOOD_NAME WHERE ");
        for (int i = 0; i < wordsToUse; i++) {
            sql.append("FoodDescription REGEXP ?");
            if (i < wordsToUse - 1) {
                sql.append(" AND ");
            }
        }
        sql.append(" ORDER BY ");
        sql.append("CASE ");
        sql.append("    WHEN FoodDescription LIKE '%raw%' THEN 0 ");
        sql.append("    WHEN FoodDescription NOT LIKE '%cooked%' AND FoodDescription NOT LIKE '%canned%' AND FoodDescription NOT LIKE '%frozen%' AND FoodDescription NOT LIKE '%sauce%' AND FoodDescription NOT LIKE '%soup%' AND FoodDescription NOT LIKE '%dish%' THEN 1 ");
        sql.append("    ELSE 2 ");
        sql.append("END ASC, ");
        sql.append("LENGTH(FoodDescription) ASC ");
        sql.append("LIMIT 1");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < wordsToUse; i++) {
                String sanitizedWord = words[i].replaceAll("([\\\\\\.\\[\\]\\{\\}\\(\\)\\*\\+\\?\\^\\$\\|])", "\\\\$1");
                pstmt.setString(i + 1, "\\b" + sanitizedWord + "\\b");
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("FoodID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.err.println("WARN: No matching food found in the database for: '" + description + "'");
        return -1;
    }

    public String getFoodGroup(String fullIngredientLine) {
        Matcher matcher = ingredientPattern.matcher(fullIngredientLine.trim());
        if (!matcher.matches()) {
            return null;
        }
        String description = matcher.group(2).trim();
        int foodId = findFoodId(description);

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
        Map<String, Double> nutrients = new HashMap<>();
        int foodId = findFoodId(foodDescription);
        if (foodId == -1) return nutrients;

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
        Map<String, Double> nutrients = new HashMap<>();
        int foodId = findFoodId(foodDescription);
        if (foodId == -1) return nutrients;

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
}