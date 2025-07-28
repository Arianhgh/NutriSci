package com.nutri_sci.service;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.Goal;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.SwapSuggestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the SwapEngine service class.
 * <p>
 * This test class validates the functionality of the SwapEngine, which is responsible
 * for finding and evaluating food swap recommendations based on nutritional goals.
 * The tests ensure that the swap algorithm correctly identifies suitable alternatives,
 * applies nutritional constraints, and generates proper swap suggestions.
 * </p>
 * <p>
 * Test coverage includes:
 * <ul>
 *   <li>Goal-based swap suggestion generation</li>
 *   <li>Nutritional constraint evaluation</li>
 *   <li>Meal modification and ingredient replacement</li>
 *   <li>Scoring algorithm validation</li>
 * </ul>
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.service.SwapEngine
 */
class SwapEngineTest {

    /** The SwapEngine instance under test */
    @InjectMocks
    private SwapEngine swapEngine;

    /** Mock database manager for isolating service logic from database operations */
    @Mock
    private DBManager dbManager;

    /** Mock nutrient calculator for predictable test results */
    @Mock
    private NutrientCalculator nutrientCalculator;

    /**
     * Sets up the test environment before each test method.
     * <p>
     * Initializes mocks and injects them into the SwapEngine instance.
     * This ensures each test starts with a clean, predictable state.
     * </p>
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests swap suggestion generation for a decrease calories goal.
     * <p>
     * This test verifies that the SwapEngine correctly identifies foods with lower
     * calorie content when given a goal to decrease calories. It validates that
     * the algorithm properly compares nutritional profiles and suggests appropriate
     * alternatives that move in the correct direction for the specified goal.
     * </p>
     */
    @Test
    void findSwaps_DecreaseCaloriesGoal_FindsLowerCalorieItem() {
        // Arrange
        Meal originalMeal = new Meal();
        originalMeal.setIngredients("100g White Bread");
        String itemToSwap = "100g White Bread";
        List<Goal> goals = Collections.singletonList(new Goal("Calories", "Decrease", 10, true));

        // Mock nutrient profiles
        Map<String, Double> whiteBreadNutrients = new HashMap<>();
        whiteBreadNutrients.put("Calories", 265.0);
        whiteBreadNutrients.put("Protein", 9.0);

        Map<String, Double> wholeWheatNutrients = new HashMap<>();
        wholeWheatNutrients.put("Calories", 247.0); // Lower calories
        wholeWheatNutrients.put("Protein", 13.0);

        when(dbManager.getNutrientProfile("White Bread")).thenReturn(whiteBreadNutrients);
        when(dbManager.getNutrientProfile("Whole Wheat Bread")).thenReturn(wholeWheatNutrients);
        when(dbManager.getFoodsByNutrientRank(eq("Calories"), eq("LOW"))).thenReturn(Collections.singletonList("Whole Wheat Bread"));

        // Act
        List<SwapSuggestion> suggestions = swapEngine.findSwaps(originalMeal, itemToSwap, goals, 20, false, false);

        // Assert
        assertFalse(suggestions.isEmpty());
        assertEquals("Whole Wheat Bread", suggestions.get(0).getFoodName());
    }

    /**
     * Tests meal modification through ingredient replacement.
     * <p>
     * This test verifies that the SwapEngine correctly applies a food swap to a meal,
     * replacing the specified ingredient while maintaining the same quantity and
     * recalculating the nutritional values for the entire modified meal. It ensures
     * that other ingredients in the meal remain unchanged.
     * </p>
     */
    @Test
    void performSwap_ReplacesIngredientAndRecalculates() {
        // Arrange
        Meal originalMeal = new Meal();
        originalMeal.setIngredients("100g White Bread\n10g Butter");
        String itemToSwap = "100g White Bread";
        String newItem = "Whole Wheat Bread";

        Map<String, Double> newNutrients = new HashMap<>();
        newNutrients.put("ENERGY (KILOCALORIES)", 320.0);
        when(nutrientCalculator.calculateNutrientsForMeal(anyString())).thenReturn(newNutrients);

        // Act
        Meal swappedMeal = swapEngine.performSwap(originalMeal, itemToSwap, newItem);

        // Assert
        assertTrue(swappedMeal.getIngredients().contains("100g Whole Wheat Bread"));
        assertFalse(swappedMeal.getIngredients().contains("100g White Bread"));
        assertEquals(320.0, swappedMeal.getEstimatedCalories(), 0.001);
    }
}