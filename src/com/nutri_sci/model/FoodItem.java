package com.nutri_sci.model;

/**
 * Represents a single food item suggestion from the nutrition database.
 * <p>
 * This class is used to display food choices to the user during ingredient
 * clarification and resolution. When a user enters an ingredient that matches
 * multiple foods in the database, FoodItem instances provide the available
 * options for the user to choose from.
 * </p>
 * <p>
 * Each FoodItem contains a unique database identifier and a human-readable
 * description that can be displayed in user interface components.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.ui.IngredientResolverDialog
 */
public class FoodItem {
    
    /** Unique database identifier for the food item */
    private final int foodId;
    
    /** Human-readable description of the food item */
    private final String description;

    /**
     * Constructs a new FoodItem with the specified ID and description.
     * 
     * @param foodId the unique database identifier for this food item
     * @param description the human-readable name/description of the food item
     */
    public FoodItem(int foodId, String description) {
        this.foodId = foodId;
        this.description = description;
    }

    /**
     * Gets the unique database identifier for this food item.
     * 
     * @return the food ID from the nutrition database
     */
    public int getFoodId() {
        return foodId;
    }

    /**
     * Gets the human-readable description of this food item.
     * 
     * @return the food description/name
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the string representation of this food item for display purposes.
     * <p>
     * This method is commonly used by UI components like JList and JComboBox
     * to display the food item description to the user.
     * </p>
     * 
     * @return the food description
     */
    @Override
    public String toString() {
        return description;
    }
}