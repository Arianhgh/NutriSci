package com.nutri_sci.database;

import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;

import java.sql.*;
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
            System.out.println("DBManager connected to the database successfully.");

            // Ensures necessary tables exist on startup.
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
        // `IF NOT EXISTS` prevents errors if the tables are already present.
        String createUserProfileTable = "CREATE TABLE IF NOT EXISTS USER_PROFILE ("
                + "UserID INT PRIMARY KEY AUTO_INCREMENT,"
                + "ProfileName VARCHAR(255) NOT NULL UNIQUE,"
                + "Sex VARCHAR(50),"
                + "DateOfBirth DATE,"
                + "HeightCM DOUBLE,"
                + "WeightKG DOUBLE"
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
                // Ensures data integrity by cascading deletes from USER_PROFILE.
                + "FOREIGN KEY (UserID) REFERENCES USER_PROFILE(UserID) ON DELETE CASCADE,"
                // A swapped meal can be deleted without affecting its replacement.
                + "FOREIGN KEY (OriginalMealID) REFERENCES MEAL_LOG(MealID) ON DELETE SET NULL"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            System.out.println("Verifying application tables...");
            stmt.executeUpdate(createUserProfileTable);
            stmt.executeUpdate(createMealLogTable);
            System.out.println("Application tables verified successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create or verify application tables.", e);
        }
    }

    /**
     * Provides global access to the single DBManager instance, creating it if necessary.
     * `synchronized` makes this method thread-safe.
     */
    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
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
        // Dynamically set the sort order based on user selection.
        String sortOrder = rank.equalsIgnoreCase("HIGH") ? "DESC" : "ASC";
        String sql = "SELECT FN.FoodDescription FROM NUTRIENT_AMOUNT NA " +
                "JOIN FOOD_NAME FN ON NA.FoodID = FN.FoodID " +
                "WHERE NA.NutrientID = ? " +
                "ORDER BY NA.NutrientValue " + sortOrder + " " +
                "LIMIT 100"; // Limit results for performance.

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
        String sql = "INSERT INTO USER_PROFILE (ProfileName, Sex, DateOfBirth, HeightCM, WeightKG) VALUES (?, ?, ?, ?, ?)";
        // `RETURN_GENERATED_KEYS` is used to retrieve the auto-incremented UserID after insert.
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, profile.getName());
            pstmt.setString(2, profile.getSex());
            pstmt.setDate(3, new java.sql.Date(profile.getDateOfBirth().getTime()));
            pstmt.setDouble(4, profile.getHeight());
            pstmt.setDouble(5, profile.getWeight());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                profile.setId(generatedKeys.getInt(1)); // Set the new ID on the profile object.
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
                // Explicitly set the nullable integer foreign key to NULL.
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Meal> getMealsForUser(int userId) {
        List<Meal> meals = new ArrayList<>();
        // This subquery ensures that we don't show meals that have been swapped out for another.
        String sql = "SELECT * FROM MEAL_LOG WHERE UserID = ? AND MealID NOT IN (SELECT OriginalMealID FROM MEAL_LOG WHERE OriginalMealID IS NOT NULL) ORDER BY MealDate DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Meal meal = new Meal();
                meal.setMealId(rs.getInt("MealID"));
                meal.setDate(rs.getTimestamp("MealDate"));
                meal.setMealType(rs.getString("MealType"));
                meal.setIngredients(rs.getString("Ingredients"));
                meal.setEstimatedCalories(rs.getDouble("EstimatedCalories"));
                meal.setSwapped(rs.getBoolean("IsSwapped"));
                // `getObject` is used to safely retrieve a nullable integer column.
                meal.setOriginalMealId((Integer) rs.getObject("OriginalMealID"));
                meals.add(meal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meals;
    }

    public boolean hasUserLoggedMealTypeOnDate(int userId, String mealType, java.util.Date date) {
        // `DATE(MealDate)` function ignores the time part of the DATETIME column for comparison.
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

    /**
     * Attempts to find the best matching food item in the database based on a description.
     */
    public int findFoodId(String description) {
        String sanitizedDescription = description.trim().replace(",", "");
        String[] words = sanitizedDescription.split("\\s+");
        if (words.length == 0) return -1;

        // This SQL query is complex, using REGEXP to match all words in the description.
        StringBuilder sql = new StringBuilder("SELECT FoodID, FoodDescription FROM FOOD_NAME WHERE ");
        for (int i = 0; i < words.length; i++) {
            sql.append("FoodDescription REGEXP ?");
            if (i < words.length - 1) {
                sql.append(" AND ");
            }
        }
        // The ORDER BY clause prioritizes more basic/raw forms of food and shorter descriptions.
        sql.append(" ORDER BY ");
        sql.append("CASE ");
        sql.append("    WHEN FoodDescription LIKE '%raw%' THEN 0 "); // Highest priority
        sql.append("    WHEN FoodDescription NOT LIKE '%cooked%' AND FoodDescription NOT LIKE '%canned%' AND FoodDescription NOT LIKE '%frozen%' AND FoodDescription NOT LIKE '%sauce%' AND FoodDescription NOT LIKE '%soup%' AND FoodDescription NOT LIKE '%dish%' THEN 1 "); // Second priority
        sql.append("    ELSE 2 "); // Lowest priority
        sql.append("END ASC, ");
        sql.append("LENGTH(FoodDescription) ASC "); // Prefer shorter, more specific names
        sql.append("LIMIT 1");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < words.length; i++) {
                // Escape any special regex characters in the user's input word.
                String sanitizedWord = words[i].replaceAll("([\\\\\\.\\[\\]\\{\\}\\(\\)\\*\\+\\?\\^\\$\\|])", "\\\\$1");
                // `\b` creates a word boundary to avoid matching substrings (e.g., 'app' in 'apple').
                pstmt.setString(i + 1, "\\b" + sanitizedWord + "\\b");
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("INFO: For ingredient '" + description + "', found DB entry: '" + rs.getString("FoodDescription") + "'");
                return rs.getInt("FoodID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.err.println("WARN: No matching food found in the database for: '" + description + "'");
        return -1;
    }

    public String getFoodGroup(String fullIngredientLine) {
        // Use the regex pattern to extract the food description from the ingredient line.
        Matcher matcher = ingredientPattern.matcher(fullIngredientLine.trim());
        if (!matcher.matches()) {
            return null;
        }
        String description = matcher.group(2).trim(); // Group 2 is the food description part.
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
                // Map the nutrient IDs back to human-readable names.
                if (nutrientId == CALORIE_NUTRIENT_ID) nutrients.put("Calories", value);
                else if (nutrientId == PROTEIN_NUTRIENT_ID) nutrients.put("Protein", value);
                else if (nutrientId == FIBER_NUTRIENT_ID) nutrients.put("Fiber", value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nutrients;
    }
}