package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.UserProfile;

import javax.swing.*;
import java.util.Date;

/**
 * Handles business logic for creating and managing user profiles.
 */
public class ProfileController {
    private final DBManager dbManager;

    public ProfileController() {
        // Use Singleton pattern to get the database manager instance.
        this.dbManager = DBManager.getInstance();
    }

    /**
     * Creates and saves a new user profile after validation.
     *
     * @param name      User's full name.
     * @param sex       User's sex.
     * @param dob       User's date of birth.
     * @param heightStr User's height as a string.
     * @param weightStr User's weight as a string.
     * @return The created UserProfile object on success, or null on failure.
     */
    public UserProfile createProfile(String name, String sex, Date dob, String heightStr, String weightStr) {
        // Basic validation to ensure no fields are empty.
        if (name.trim().isEmpty() || sex == null || dob == null || heightStr.trim().isEmpty() || weightStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            UserProfile profile = new UserProfile();
            profile.setName(name);
            profile.setSex(sex);
            profile.setDateOfBirth(dob);
            // Parse string inputs for height and weight; this can throw an exception.
            profile.setHeight(Double.parseDouble(heightStr));
            profile.setWeight(Double.parseDouble(weightStr));

            dbManager.saveProfile(profile);
            JOptionPane.showMessageDialog(null, "Profile created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return profile;

        } catch (NumberFormatException e) {
            // Handle cases where height or weight are not valid numerical values.
            JOptionPane.showMessageDialog(null, "Height and Weight must be valid numbers.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}