package com.nutri_sci.database;

import com.nutri_sci.model.FoodItem;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages database connections and provides access to repository classes.
 * Refactored to use composition with repository pattern for better separation of concerns.
 */
public class DBManager {
    
    /** The singleton instance of DBManager */
    private static DBManager instance;
    
    /** Database connection instance */
    private Connection connection;

    /** Database connection URL */
    private static final String DB_URL = "jdbc:mysql://localhost/nutrisci_db";
    
    /** Database username */
    private static final String USER = "root";
    
    /** Database password */
    private static final String PASS = "root";

    // Repository instances
    private UserProfileRepository userProfileRepository;
    private MealRepository mealRepository;
    private FoodRepository foodRepository;

    /**
     * Private constructor to prevent direct instantiation (Singleton pattern).
     */
    private DBManager() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            createApplicationTables();
            initializeRepositories();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the database.");
        }
    }

    /**
     * Initializes the repository instances with the database connection.
     */
    private void initializeRepositories() {
        this.userProfileRepository = new UserProfileRepository(connection);
        this.mealRepository = new MealRepository(connection);
        this.foodRepository = new FoodRepository(connection);
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

    // Delegate methods to repositories - UserProfile operations
    public UserProfile saveProfile(UserProfile profile) {
        return userProfileRepository.saveProfile(profile);
    }

    public UserProfile getProfile(String profileName) {
        return userProfileRepository.getProfile(profileName);
    }

    public Set<String> getAllUserNames() {
        return userProfileRepository.getAllUserNames();
    }

    public boolean updateProfile(UserProfile profile) {
        return userProfileRepository.updateProfile(profile);
    }

    // Delegate methods to repositories - Meal operations
    public Date getMostRecentMealDate(int userId) {
        return mealRepository.getMostRecentMealDate(userId);
    }

    public boolean saveMeal(int userId, Meal meal) {
        return mealRepository.saveMeal(userId, meal);
    }

    public List<Meal> getMealsForUser(int userId) {
        return mealRepository.getMealsForUser(userId);
    }

    public List<Meal> getMealsForUser(int userId, Date startDate, Date endDate) {
        return mealRepository.getMealsForUser(userId, startDate, endDate);
    }

    public List<Meal> getMealsForUser(int userId, Date startDate, Date endDate, boolean includeReplacedMeals) {
        return mealRepository.getMealsForUser(userId, startDate, endDate, includeReplacedMeals);
    }

    public Meal getMealById(int mealId) {
        return mealRepository.getMealById(mealId);
    }

    public boolean hasUserLoggedMealTypeOnDate(int userId, String mealType, java.util.Date date) {
        return mealRepository.hasUserLoggedMealTypeOnDate(userId, mealType, date);
    }

    public boolean deleteMeal(int mealId) {
        return mealRepository.deleteMeal(mealId);
    }

    // Delegate methods to repositories - Food operations
    public List<String> getFoodsByNutrientRank(String nutrientName, String rank) {
        return foodRepository.getFoodsByNutrientRank(nutrientName, rank);
    }

    public double getCaloriesPer100g(int foodId) {
        return foodRepository.getCaloriesPer100g(foodId);
    }

    public List<FoodItem> findFoodSuggestions(String description, int limit) {
        return foodRepository.findFoodSuggestions(description, limit);
    }

    public String getFoodGroup(String fullIngredientLine) {
        return foodRepository.getFoodGroup(fullIngredientLine);
    }

    public List<String> getFoodsFromGroup(String foodGroup) {
        return foodRepository.getFoodsFromGroup(foodGroup);
    }

    public Map<String, Double> getNutrientProfile(String foodDescription) {
        return foodRepository.getNutrientProfile(foodDescription);
    }

    public Map<String, Double> getComprehensiveNutrientProfile(String foodDescription) {
        return foodRepository.getComprehensiveNutrientProfile(foodDescription);
    }

    public Map<String, Double> getFoodGroupDistribution(int userId, Date startDate, Date endDate) {
        return foodRepository.getFoodGroupDistribution(userId, startDate, endDate);
    }
}