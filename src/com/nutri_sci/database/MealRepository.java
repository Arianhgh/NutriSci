package com.nutri_sci.database;

import com.nutri_sci.model.Meal;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Repository for managing meal data persistence.
 * Extracted from DBManager to follow Single Responsibility Principle.
 */
public class MealRepository {
    private final Connection connection;

    public MealRepository(Connection connection) {
        this.connection = connection;
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
        return null;
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

    public boolean deleteMeal(int mealId) {
        String sql = "DELETE FROM MEAL_LOG WHERE MealID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, mealId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 