package com.nutri_sci.service;

import com.nutri_sci.database.DBManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the NutrientCalculator service class.
 * <p>
 * This test class validates the functionality of the NutrientCalculator, which is
 * responsible for parsing ingredient strings and calculating comprehensive nutritional
 * information for meals. The tests ensure that ingredient parsing, quantity scaling,
 * and nutrient aggregation work correctly across different input scenarios.
 * </p>
 * <p>
 * Test coverage includes:
 * <ul>
 *   <li>Multi-ingredient meal nutrient calculation</li>
 *   <li>Proportional scaling based on ingredient quantities</li>
 *   <li>Nutrient aggregation and summation</li>
 *   <li>Edge case handling for empty or invalid inputs</li>
 * </ul>
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.service.NutrientCalculator
 */
class NutrientCalculatorTest {

    /** The NutrientCalculator instance under test */
    @InjectMocks
    private NutrientCalculator nutrientCalculator;

    /** Mock database manager for isolating service logic from database operations */
    @Mock
    private DBManager dbManager;

    /**
     * Sets up the test environment before each test method.
     * <p>
     * Initializes mocks and injects them into the NutrientCalculator instance.
     * This ensures each test starts with a clean, predictable state.
     * </p>
     */
    @BeforeEach
    void setUp() {
        // Initializes the mocks and injects them into the nutrientCalculator instance
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests successful nutrient calculation for a multi-ingredient meal.
     * <p>
     * This test verifies that the NutrientCalculator correctly parses ingredient
     * strings, looks up nutritional data for each ingredient, applies quantity-based
     * scaling, and aggregates the total nutritional values. It validates that nutrients
     * from multiple ingredients are properly summed and that proportional scaling
     * based on gram quantities works accurately.
     * </p>
     */
    @Test
    void calculateNutrientsForMeal_Success() {
        // Arrange
        String ingredients = "100g Chicken Breast\n50g Brown Rice";
        Map<String, Double> chickenNutrients = new HashMap<>();
        chickenNutrients.put("Protein (g)", 25.0);
        chickenNutrients.put("Fat (g)", 3.5);

        Map<String, Double> riceNutrients = new HashMap<>();
        riceNutrients.put("Carbohydrate (g)", 23.0);
        riceNutrients.put("Protein (g)", 2.5);

        // Define mock behavior
        when(dbManager.getComprehensiveNutrientProfile("Chicken Breast")).thenReturn(chickenNutrients);
        when(dbManager.getComprehensiveNutrientProfile("Brown Rice")).thenReturn(riceNutrients);

        // Act
        Map<String, Double> totalNutrients = nutrientCalculator.calculateNutrientsForMeal(ingredients);

        // Assert
        // A small delta (tolerance) of 0.001 is added to each assertion
        // to account for any potential floating-point inaccuracies.
        assertEquals(26.25, totalNutrients.get("Protein (g)"), 0.001);
        assertEquals(3.5, totalNutrients.get("Fat (g)"), 0.001);
        assertEquals(11.5, totalNutrients.get("Carbohydrate (g)"), 0.001);
    }

    /**
     * Tests nutrient calculation behavior with empty ingredient input.
     * <p>
     * This test verifies that the NutrientCalculator handles edge cases gracefully
     * when provided with empty or null ingredient strings. It should return an
     * empty nutrient map without throwing exceptions, ensuring robust error handling
     * for invalid input scenarios.
     * </p>
     */
    @Test
    void calculateNutrientsForMeal_EmptyIngredients() {
        // Act
        Map<String, Double> totalNutrients = nutrientCalculator.calculateNutrientsForMeal("");

        // Assert
        assertEquals(0, totalNutrients.size());
    }
}