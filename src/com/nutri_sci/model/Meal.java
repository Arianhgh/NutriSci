package com.nutri_sci.model;

import java.util.Date;
import java.util.Map;

public class Meal {
    private int mealId;
    private Date date;
    private String mealType;
    private String ingredients;
    private double estimatedCalories;
    private Map<String, Double> nutrientBreakdown;
    private boolean isSwapped = false;
    private Integer originalMealId = null;

    // Getters and Setters
    public int getMealId() { return mealId; }
    public void setMealId(int mealId) { this.mealId = mealId; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public double getEstimatedCalories() { return estimatedCalories; }
    public void setEstimatedCalories(double estimatedCalories) { this.estimatedCalories = estimatedCalories; }
    public Map<String, Double> getNutrientBreakdown() { return nutrientBreakdown; }
    public void setNutrientBreakdown(Map<String, Double> nutrientBreakdown) { this.nutrientBreakdown = nutrientBreakdown; }
    public boolean isSwapped() { return isSwapped; }
    public void setSwapped(boolean swapped) { isSwapped = swapped; }
    public Integer getOriginalMealId() { return originalMealId; }
    public void setOriginalMealId(Integer originalMealId) { this.originalMealId = originalMealId; }
}