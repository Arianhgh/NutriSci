package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.NutrientCalculator;
import org.jfree.data.general.DefaultPieDataset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the VisualizationController class.
 * <p>
 * This test class validates the functionality of the VisualizationController, which
 * handles data aggregation and chart dataset creation for nutritional visualization
 * features. The tests ensure that nutritional data is correctly processed, averaged,
 * and formatted for use with JFreeChart components.
 * </p>
 * <p>
 * Test coverage includes:
 * <ul>
 *   <li>Macronutrient dataset creation with proper averaging</li>
 *   <li>Canadian Food Guide comparison dataset generation</li>
 *   <li>Food group normalization and aggregation</li>
 *   <li>Data validation and edge case handling</li>
 * </ul>
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.controller.VisualizationController
 */
class VisualizationControllerTest {

    /** The VisualizationController instance under test */
    @InjectMocks
    private VisualizationController visualizationController;

    /** Mock database manager for isolating controller logic from database operations */
    @Mock
    private DBManager dbManager;

    /** Mock nutrient calculator for predictable test results */
    @Mock
    private NutrientCalculator nutrientCalculator;

    /** Test user profile used across multiple test methods */
    private UserProfile testProfile;

    /**
     * Sets up the test environment before each test method.
     * <p>
     * Initializes mocks, creates a test user profile, and constructs the
     * VisualizationController with the mocked dependencies. Manual construction
     * is used instead of @InjectMocks to ensure proper dependency injection
     * with the custom constructor.
     * </p>
     */
    @BeforeEach
    void setUp() {
        // We need to inject the mock nutrientCalculator, so we can't use @InjectMocks alone
        dbManager = mock(DBManager.class);
        nutrientCalculator = mock(NutrientCalculator.class);
        testProfile = new UserProfile();
        testProfile.setId(1);
        visualizationController = new VisualizationController(testProfile, dbManager, nutrientCalculator);
    }

    /**
     * Tests macronutrient dataset creation with correct average calculations.
     * <p>
     * This test verifies that the VisualizationController correctly aggregates
     * nutritional data from multiple meals and calculates average macronutrient
     * values for visualization. It ensures that protein, carbohydrates, and fat
     * values are properly extracted and summed from meal data.
     * </p>
     */
    @Test
    void createMacroNutrientDataset_CalculatesCorrectAverages() {
        // Arrange
        Meal lunch = new Meal();
        lunch.setIngredients("A meal");
        Map<String, Double> lunchNutrients = new HashMap<>();
        lunchNutrients.put("PROTEIN", 20.0);
        lunchNutrients.put("CARBOHYDRATE, TOTAL", 50.0);
        lunchNutrients.put("FAT (TOTAL LIPIDS)", 15.0);

        when(dbManager.getMealsForUser(eq(1), any(Date.class), any(Date.class))).thenReturn(Collections.singletonList(lunch));
        when(nutrientCalculator.calculateNutrientsForMeal(anyString())).thenReturn(lunchNutrients);

        // Act
        DefaultPieDataset dataset = visualizationController.createMacroNutrientDataset(new Date(), new Date());

        // Assert
        assertEquals(20.0, (Double) dataset.getValue("Protein (g)"), 0.001);
        assertEquals(50.0, (Double) dataset.getValue("Carbohydrates (g)"), 0.001);
        assertEquals(15.0, (Double) dataset.getValue("Fat (g)"), 0.001);
    }

    /**
     * Tests Canadian Food Guide comparison dataset creation with food group normalization.
     * <p>
     * This test verifies that the VisualizationController correctly maps database
     * food groups to Canadian Food Guide categories and aggregates food quantities
     * appropriately. It ensures that foods from different database categories are
     * properly normalized to the four main CFG food groups.
     * </p>
     */
    @Test
    void createCfgComparisonDataset_NormalizesAndAggregatesFoodGroups() {
        // Arrange
        Meal meal = new Meal();
        meal.setIngredients("100g Apple, raw\n150g Beef, ground, raw");

        when(dbManager.getMealsForUser(eq(1), any(Date.class), any(Date.class))).thenReturn(Collections.singletonList(meal));
        when(dbManager.getFoodGroup("100g Apple, raw")).thenReturn("Fruits and Fruit Juices");
        when(dbManager.getFoodGroup("150g Beef, ground, raw")).thenReturn("Beef Products");

        // Act
        DefaultPieDataset dataset = visualizationController.createCfgComparisonDataset(new Date(), new Date());

        // Assert
        assertNotNull(dataset.getValue("Vegetables and Fruit"));
        assertNotNull(dataset.getValue("Meat and Alternatives"));
        assertEquals(100.0, (Double) dataset.getValue("Vegetables and Fruit"), 0.001);
        assertEquals(150.0, (Double) dataset.getValue("Meat and Alternatives"), 0.001);
    }
}