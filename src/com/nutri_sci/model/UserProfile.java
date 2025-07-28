package com.nutri_sci.model;

import java.util.Date;

/**
 * Represents a user profile containing personal information and preferences
 * for the NutriSci application.
 * <p>
 * This class stores user demographic data, physical measurements, and preferences
 * that are used for personalized nutritional recommendations and goal setting.
 * The profile supports both metric and imperial measurement systems.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 */
public class UserProfile {
    
    /** Database primary key identifier for the user profile */
    private int id;
    
    /** User's full name */
    private String name;
    
    /** User's biological sex (used for nutritional calculations) */
    private String sex;
    
    /** User's date of birth (used for age-based nutritional recommendations) */
    private Date dateOfBirth;
    
    /** User's height in the specified measurement unit */
    private double height;
    
    /** User's weight in the specified measurement unit */
    private double weight;
    
    /** Measurement system preference: "Metric" (cm, kg) or "Imperial" (inches, lbs) */
    private String measurementUnit;

    /**
     * Gets the database ID of the user profile.
     * 
     * @return the unique database identifier
     */
    public int getId() { return id; }
    
    /**
     * Sets the database ID of the user profile.
     * 
     * @param id the unique database identifier
     */
    public void setId(int id) { this.id = id; }
    
    /**
     * Gets the user's full name.
     * 
     * @return the user's name
     */
    public String getName() { return name; }
    
    /**
     * Sets the user's full name.
     * 
     * @param name the user's name
     */
    public void setName(String name) { this.name = name; }
    
    /**
     * Gets the user's biological sex.
     * 
     * @return the user's sex (typically "Male" or "Female")
     */
    public String getSex() { return sex; }
    
    /**
     * Sets the user's biological sex.
     * 
     * @param sex the user's sex (used for nutritional calculations)
     */
    public void setSex(String sex) { this.sex = sex; }
    
    /**
     * Gets the user's date of birth.
     * 
     * @return the birth date
     */
    public Date getDateOfBirth() { return dateOfBirth; }
    
    /**
     * Sets the user's date of birth.
     * 
     * @param dateOfBirth the birth date (used for age-based recommendations)
     */
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    /**
     * Gets the user's height.
     * 
     * @return the height in the specified measurement unit
     */
    public double getHeight() { return height; }
    
    /**
     * Sets the user's height.
     * 
     * @param height the height in the specified measurement unit
     */
    public void setHeight(double height) { this.height = height; }
    
    /**
     * Gets the user's weight.
     * 
     * @return the weight in the specified measurement unit
     */
    public double getWeight() { return weight; }
    
    /**
     * Sets the user's weight.
     * 
     * @param weight the weight in the specified measurement unit
     */
    public void setWeight(double weight) { this.weight = weight; }
    
    /**
     * Gets the measurement unit preference.
     * 
     * @return "Metric" for cm/kg or "Imperial" for inches/lbs
     */
    public String getMeasurementUnit() { return measurementUnit; }
    
    /**
     * Sets the measurement unit preference.
     * 
     * @param measurementUnit "Metric" for cm/kg or "Imperial" for inches/lbs
     */
    public void setMeasurementUnit(String measurementUnit) { this.measurementUnit = measurementUnit; }
}