package com.nutri_sci.model;

/**
 * A data class representing a single nutritional goal for the SwapEngine.
 */
public class Goal {
    private final String nutrient;
    private final String type; // "Increase" or "Decrease"
    private final double value;
    private final boolean isRelative; // true for percentage, false for absolute

    public Goal(String nutrient, String type, double value, boolean isRelative) {
        this.nutrient = nutrient;
        this.type = type;
        this.value = value;
        this.isRelative = isRelative;
    }

    // Getters
    public String getNutrient() { return nutrient; }
    public String getType() { return type; }
    public double getValue() { return value; }
    public boolean isRelative() { return isRelative; }
}