package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.ProfileData;
import com.nutri_sci.model.UserProfile;

import javax.swing.*;
import java.util.Date;

/**
 * Controller for managing user profile operations and validation.
 * Refactored to use ProfileData parameter object for cleaner method signatures.
 */
public class ProfileController {
    
    /** Database manager for profile persistence operations */
    private final DBManager dbManager;

    public ProfileController() {
        this.dbManager = DBManager.getInstance();
    }

    /**
     * Creates a new user profile with validation and persistence.
     * Refactored to use ProfileData parameter object.
     */
    public UserProfile createProfile(ProfileData profileData) {
        if (!profileData.isValid()) {
            JOptionPane.showMessageDialog(null, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            UserProfile profile = new UserProfile();
            profile.setName(profileData.getName());
            profile.setSex(profileData.getSex());
            profile.setDateOfBirth(profileData.getDateOfBirth());
            profile.setHeight(profileData.getHeightAsDouble());
            profile.setWeight(profileData.getWeightAsDouble());
            profile.setMeasurementUnit(profileData.getUnit());

            dbManager.saveProfile(profile);
            JOptionPane.showMessageDialog(null, "Profile created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return profile;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Height and Weight must be valid numbers.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Legacy method for backwards compatibility.
     * @deprecated Use createProfile(ProfileData) instead
     */
    @Deprecated
    public UserProfile createProfile(String name, String sex, Date dob, String heightStr, String weightStr, String unit) {
        ProfileData profileData = new ProfileData(name, sex, dob, heightStr, weightStr, unit);
        return createProfile(profileData);
    }
    
    /**
     * Updates an existing user profile with new information.
     * Refactored to use ProfileData parameter object.
     */
    public boolean updateProfile(UserProfile profile, ProfileData profileData) {
        if (!profileData.isValid()) {
            JOptionPane.showMessageDialog(null, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            profile.setName(profileData.getName());
            profile.setSex(profileData.getSex());
            profile.setDateOfBirth(profileData.getDateOfBirth());
            profile.setHeight(profileData.getHeightAsDouble());
            profile.setWeight(profileData.getWeightAsDouble());
            profile.setMeasurementUnit(profileData.getUnit());

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

    /**
     * Legacy method for backwards compatibility.
     * @deprecated Use updateProfile(UserProfile, ProfileData) instead
     */
    @Deprecated
    public boolean updateProfile(UserProfile profile, String name, String sex, Date dob, String heightStr, String weightStr, String unit) {
        ProfileData profileData = new ProfileData(name, sex, dob, heightStr, weightStr, unit);
        return updateProfile(profile, profileData);
    }
}