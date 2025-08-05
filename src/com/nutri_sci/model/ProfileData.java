package com.nutri_sci.model;

import java.util.Date;

/**
 * Data transfer object for profile information.
 * Created to reduce long parameter lists in ProfileController (Introduce Parameter Object refactoring).
 */
public class ProfileData {
    private String name;
    private String sex;
    private Date dateOfBirth;
    private String heightStr;
    private String weightStr;
    private String unit;

    /**
     * Constructs a new ProfileData object with all profile fields.
     */
    public ProfileData(String name, String sex, Date dateOfBirth, String heightStr, String weightStr, String unit) {
        this.name = name;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.heightStr = heightStr;
        this.weightStr = weightStr;
        this.unit = unit;
    }

    // Getters
    public String getName() { return name; }
    public String getSex() { return sex; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public String getHeightStr() { return heightStr; }
    public String getWeightStr() { return weightStr; }
    public String getUnit() { return unit; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setSex(String sex) { this.sex = sex; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setHeightStr(String heightStr) { this.heightStr = heightStr; }
    public void setWeightStr(String weightStr) { this.weightStr = weightStr; }
    public void setUnit(String unit) { this.unit = unit; }

    /**
     * Validates that all required fields are present and non-empty.
     * @return true if all fields are valid, false otherwise
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               sex != null &&
               dateOfBirth != null &&
               heightStr != null && !heightStr.trim().isEmpty() &&
               weightStr != null && !weightStr.trim().isEmpty();
    }

    /**
     * Parses height as double, throwing NumberFormatException if invalid.
     * @return parsed height value
     * @throws NumberFormatException if height cannot be parsed
     */
    public double getHeightAsDouble() throws NumberFormatException {
        return Double.parseDouble(heightStr);
    }

    /**
     * Parses weight as double, throwing NumberFormatException if invalid.
     * @return parsed weight value
     * @throws NumberFormatException if weight cannot be parsed
     */
    public double getWeightAsDouble() throws NumberFormatException {
        return Double.parseDouble(weightStr);
    }
} 