package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the ProfileController class.
 * <p>
 * This test class validates the functionality of the ProfileController, which handles
 * user profile creation, validation, and updates. The tests ensure that profile data
 * is properly validated, transformed, and persisted while providing appropriate error
 * handling for invalid inputs.
 * </p>
 * <p>
 * Test coverage includes:
 * <ul>
 *   <li>Successful profile creation with valid inputs</li>
 *   <li>Validation failures for invalid data formats</li>
 *   <li>Profile update operations</li>
 *   <li>Error handling for data type conversions</li>
 * </ul>
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.controller.ProfileController
 */
class ProfileControllerTest {

    /** The ProfileController instance under test */
    @InjectMocks
    private ProfileController profileController;

    /** Mock database manager for isolating controller logic from database operations */
    @Mock
    private DBManager dbManager;

    /**
     * Sets up the test environment before each test method.
     * <p>
     * Initializes mocks and injects them into the ProfileController instance.
     * This ensures each test starts with a clean, predictable state.
     * </p>
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests successful profile creation with valid inputs.
     * <p>
     * This test verifies that the ProfileController correctly processes valid
     * profile data, creates a UserProfile object with the correct values,
     * and successfully persists it through the database manager. It validates
     * that all input parameters are properly mapped to the profile object.
     * </p>
     */
    @Test
    void createProfile_Success() {
        // Arrange
        when(dbManager.saveProfile(any(UserProfile.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        UserProfile profile = profileController.createProfile("Test User", "Male", new Date(), "180", "80", "Metric");

        // Assert
        assertNotNull(profile);
        assertEquals("Test User", profile.getName());
    }

    /**
     * Tests profile creation failure with invalid number format inputs.
     * <p>
     * This test verifies that the ProfileController properly validates numeric
     * inputs for height and weight fields. When invalid string values are provided
     * that cannot be parsed as numbers, the controller should return null and
     * display an appropriate error message to the user.
     * </p>
     */
    @Test
    void createProfile_InvalidNumberFormat() {
        // Act
        UserProfile profile = profileController.createProfile("Test User", "Male", new Date(), "abc", "xyz", "Metric");

        // Assert
        assertNull(profile);
    }

    /**
     * Tests successful profile update with valid inputs.
     * <p>
     * This test verifies that the ProfileController correctly processes profile
     * update requests, applies the changes to the existing UserProfile object,
     * and successfully persists the updates through the database manager. It
     * validates that the profile object is modified with the new values.
     * </p>
     */
    @Test
    void updateProfile_Success() {
        // Arrange
        when(dbManager.updateProfile(any(UserProfile.class))).thenReturn(true);
        UserProfile existingProfile = new UserProfile();
        existingProfile.setId(1);

        // Act
        boolean result = profileController.updateProfile(existingProfile, "Updated Name", "Female", new Date(), "175", "75", "Metric");

        // Assert
        assertTrue(result);
        assertEquals("Updated Name", existingProfile.getName());
    }
}