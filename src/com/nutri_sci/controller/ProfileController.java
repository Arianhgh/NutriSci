package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.UserProfile;

import javax.swing.*;
import java.util.Date;

/**
 * Controller for managing user profile operations and validation.
 * <p>
 * The ProfileController handles all business logic related to user profile
 * management, including creation, validation, and updates of user profiles.
 * It serves as an intermediary between the user interface and the database
 * layer, ensuring data integrity and providing user feedback.
 * </p>
 * <p>
 * Key responsibilities include:
 * <ul>
 *   <li>Validating user input for profile data</li>
 *   <li>Creating new user profiles with proper validation</li>
 *   <li>Updating existing profile information</li>
 *   <li>Handling data type conversions and error reporting</li>
 *   <li>Coordinating with the database layer for persistence</li>
 * </ul>
 * </p>
 * 
 * @author NutriSci Development Team
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.model.UserProfile
 * @see com.nutri_sci.database.DBManager
 */
public class ProfileController {
    
    /** Database manager for profile persistence operations */
    private final DBManager dbManager;

    /**
     * Constructs a new ProfileController with database access.
     * <p>
     * Initializes the controller with a reference to the singleton database
     * manager for handling profile persistence operations.
     * </p>
     */
    public ProfileController() {
        this.dbManager = DBManager.getInstance();
    }

    /**
     * Creates a new user profile with validation and persistence.
     * <p>
     * This method performs comprehensive validation of all profile fields,
     * creates a new UserProfile object if validation passes, and persists
     * it to the database. User feedback is provided through dialog messages
     * for both success and error conditions.
     * </p>
     * 
     * @param name the user's full name (required, non-empty)
     * @param sex the user's biological sex (required for nutritional calculations)
     * @param dob the user's date of birth (required)
     * @param heightStr the user's height as a string (must be parseable as double)
     * @param weightStr the user's weight as a string (must be parseable as double)
     * @param unit the measurement unit system ("Metric" or "Imperial")
     * @return the created UserProfile object if successful, null if validation failed
     */
    public UserProfile createProfile(String name, String sex, Date dob, String heightStr, String weightStr, String unit) {
        if (name.trim().isEmpty() || sex == null || dob == null || heightStr.trim().isEmpty() || weightStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            UserProfile profile = new UserProfile();
            profile.setName(name);
            profile.setSex(sex);
            profile.setDateOfBirth(dob);
            profile.setHeight(Double.parseDouble(heightStr));
            profile.setWeight(Double.parseDouble(weightStr));
            profile.setMeasurementUnit(unit);

            dbManager.saveProfile(profile);
            JOptionPane.showMessageDialog(null, "Profile created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return profile;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Height and Weight must be valid numbers.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    /**
     * Updates an existing user profile with new information.
     * <p>
     * This method validates the provided profile information and updates
     * the existing profile if validation passes. The updated profile is
     * persisted to the database, and appropriate user feedback is provided.
     * </p>
     * 
     * @param profile the existing UserProfile object to update
     * @param name the updated user name (required, non-empty)
     * @param sex the updated user sex (required)
     * @param dob the updated date of birth (required)
     * @param heightStr the updated height as a string (must be parseable as double)
     * @param weightStr the updated weight as a string (must be parseable as double)
     * @param unit the updated measurement unit system ("Metric" or "Imperial")
     * @return true if the update was successful, false if validation failed or database error occurred
     */
    public boolean updateProfile(UserProfile profile, String name, String sex, Date dob, String heightStr, String weightStr, String unit) {
        if (name.trim().isEmpty() || sex == null || dob == null || heightStr.trim().isEmpty() || weightStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            profile.setName(name);
            profile.setSex(sex);
            profile.setDateOfBirth(dob);
            profile.setHeight(Double.parseDouble(heightStr));
            profile.setWeight(Double.parseDouble(weightStr));
            profile.setMeasurementUnit(unit);

            if (dbManager.updateProfile(profile)) {
                JOptionPane.showMessageDialog(null, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Height and Weight must be valid numbers.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}