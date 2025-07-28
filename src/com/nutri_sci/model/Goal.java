package com.nutri_sci.model;

/**
 * Represents a nutritional goal for the SwapEngine optimization algorithm.
 * <p>
 * A Goal defines a specific nutritional objective that the user wants to achieve
 * through food swaps. Goals can target specific nutrients (like protein, fiber, calories)
 * and specify whether to increase or decrease the nutrient amount by either an
 * absolute value or a relative percentage.
 * </p>
 * <p>
 * Goals are used by the {@link com.nutri_sci.service.SwapEngine} to evaluate
 * and score potential food swaps based on how well they achieve the user's
 * nutritional objectives.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.service.SwapEngine
 */
public class Goal {
    
    /** The target nutrient name (e.g., "Protein", "Fiber", "Calories") */
    private final String nutrient;
    
    /** The goal direction: "Increase" or "Decrease" */
    private final String type;
    
    /** The target value (either absolute amount or percentage) */
    private final double value;
    
    /** Whether the value represents a percentage (true) or absolute amount (false) */
    private final boolean isRelative;

    /**
     * Constructs a new nutritional goal.
     * 
     * @param nutrient the target nutrient name (e.g., "Protein", "Fiber", "Calories")
     * @param type the goal direction, must be "Increase" or "Decrease"
     * @param value the target value - if isRelative is true, this represents a percentage
     *              (e.g., 0.20 for 20%), otherwise an absolute amount in grams or kcal
     * @param isRelative true if the value represents a percentage change, false for absolute change
     */
    public Goal(String nutrient, String type, double value, boolean isRelative) {
        this.nutrient = nutrient;
        this.type = type;
        this.value = value;
        this.isRelative = isRelative;
    }

    /**
     * Gets the target nutrient name.
     * 
     * @return the nutrient name (e.g., "Protein", "Fiber", "Calories")
     */
    public String getNutrient() { return nutrient; }
    
    /**
     * Gets the goal direction.
     * 
     * @return "Increase" or "Decrease"
     */
    public String getType() { return type; }
    
    /**
     * Gets the target value.
     * 
     * @return the target value - percentage (0.0-1.0) if relative, absolute amount if not
     */
    public double getValue() { return value; }
    
    /**
     * Checks if this goal uses relative (percentage) or absolute values.
     * 
     * @return true if the value represents a percentage, false for absolute amounts
     */
    public boolean isRelative() { return isRelative; }
}