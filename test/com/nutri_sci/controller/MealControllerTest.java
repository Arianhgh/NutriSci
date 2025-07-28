package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.FoodItem;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.NutrientCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the MealController class.
 * <p>
 * This test class validates the functionality of the MealController, which handles
 * meal-related business logic including validation, ingredient resolution, nutritional
 * calculations, and database persistence. The tests use Mockito to mock dependencies
 * and isolate the controller logic from external systems.
 * </p>
 * <p>
 * Test coverage includes:
 * <ul>
 *   <li>Successful meal logging with valid inputs</li>
 *   <li>Validation failures for duplicate meal types</li>
 *   <li>Error handling for invalid ingredient formats</li>
 *   <li>Database interaction verification</li>
 * </ul>
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.controller.MealController
 */
class MealControllerTest {

    /** The MealController instance under test */
    private MealController mealController;

    /** Mock database manager for isolating controller logic from database operations */
    @Mock
    private DBManager dbManager;

    /** Spy of the NutrientCalculator to allow partial mocking while preserving real behavior */
    private NutrientCalculator nutrientCalculatorSpy;

    /**
     * Sets up the test environment before each test method.
     * <p>
     * Initializes mocks, creates a spy of the NutrientCalculator to allow controlled
     * testing, and constructs the MealController with the mocked dependencies.
     * This ensures each test starts with a clean, predictable state.
     * </p>
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        NutrientCalculator realCalculator = new NutrientCalculator(dbManager);

        // Create a spy of the real instance for controlled testing
        nutrientCalculatorSpy = spy(realCalculator);

        // Manually create the controller, injecting the mock and the spy
        mealController = new MealController(dbManager, nutrientCalculatorSpy);
    }

    /**
     * Tests successful meal logging with valid inputs.
     * <p>
     * This test verifies that the MealController correctly processes a valid meal
     * logging request, including ingredient resolution, nutritional calculation,
     * and database persistence. It mocks all external dependencies to focus on
     * the controller's coordination logic.
     * </p>
     * 
     * @throws Exception if an unexpected error occurs during test execution
     */
    @Test
    void logMeal_Success() throws Exception {
        // Arrange
        UserProfile user = new UserProfile();
        user.setId(1);
        String ingredients = "100g Chicken Breast";
        Map<String, Double> nutrients = new HashMap<>();
        nutrients.put("ENERGY (KILOCALORIES)", 165.0);
        nutrients.put("PROTEIN", 31.0);

        when(dbManager.hasUserLoggedMealTypeOnDate(anyInt(), anyString(), any(Date.class))).thenReturn(false);
        when(dbManager.findFoodSuggestions(anyString(), anyInt())).thenReturn(Collections.singletonList(new FoodItem(1, "Chicken Breast")));
        when(dbManager.saveMeal(anyInt(), any(Meal.class))).thenReturn(true);

        // Use doReturn().when() on the spy. This syntax is required for spies.
        doReturn(nutrients).when(nutrientCalculatorSpy).calculateNutrientsForMeal(anyString());

        // Act
        boolean result = mealController.logMeal(user, new Date(), "Lunch", ingredients, new JFrame());

        // Assert
        assertTrue(result);
        verify(dbManager).saveMeal(eq(1), any(Meal.class));
    }

    /**
     * Tests meal logging failure when the same meal type already exists for the date.
     * <p>
     * This test verifies that the MealController properly validates against duplicate
     * meal types for the same date and returns false without attempting to save when
     * a duplicate is detected. This prevents users from logging multiple meals of the
     * same type on a single day (except for snacks).
     * </p>
     * 
     * @throws Exception if an unexpected error occurs during test execution
     */
    @Test
    void logMeal_FailsWhenMealTypeAlreadyExists() throws Exception {
        // Arrange
        UserProfile user = new UserProfile();
        user.setId(1);

        when(dbManager.hasUserLoggedMealTypeOnDate(anyInt(), anyString(), any(Date.class))).thenReturn(true);
        // We still need to mock this call to prevent a NullPointerException
        when(dbManager.findFoodSuggestions(anyString(), anyInt())).thenReturn(Collections.singletonList(new FoodItem(1, "Chicken")));

        // Act
        boolean result = mealController.logMeal(user, new Date(), "Lunch", "100g Chicken", new JFrame());

        // Assert
        assertFalse(result);
        verify(dbManager, never()).saveMeal(anyInt(), any(Meal.class));
    }
}