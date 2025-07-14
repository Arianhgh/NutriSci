package com.nutri_sci.controller;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.UserProfile;

import javax.swing.*;
import java.util.Date;

public class ProfileController {
    private final DBManager dbManager;

    public ProfileController() {
        this.dbManager = DBManager.getInstance();
    }

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
}