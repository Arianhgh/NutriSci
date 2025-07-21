package com.nutri_sci.model;

/**
 * Represents a single food item suggestion from the database.
 * This is used to display choices to the user for ingredient clarification.
 */
public class FoodItem {
    private final int foodId;
    private final String description;

    public FoodItem(int foodId, String description) {
        this.foodId = foodId;
        this.description = description;
    }

    public int getFoodId() {
        return foodId;
    }

    public String getDescription() {
        return description;
    }


    @Override
    public String toString() {
        return description;
    }
}