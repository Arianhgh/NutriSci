package com.nutri_sci;

import com.nutri_sci.ui.SplashScreenUI;

import javax.swing.*;

/**
 * Main application entry point for the NutriSci application.
 * <p>
 * NutriSci (SwEATch to better!) is a nutrition tracking and meal optimization application
 * that helps users log their meals, analyze nutritional content, and discover healthier
 * food alternatives through intelligent swap suggestions.
 * </p>
 * <p>
 * The application features:
 * <ul>
 *   <li>User profile management with personalized nutritional goals</li>
 *   <li>Meal logging with detailed nutritional analysis</li>
 *   <li>Food swap recommendations based on nutritional objectives</li>
 *   <li>Data visualization and tracking capabilities</li>
 *   <li>Integration with Canadian Food Guide database</li>
 * </ul>
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 */
public class App {
    
    /**
     * Main method that initializes and launches the NutriSci application.
     * <p>
     * This method performs the following initialization steps:
     * <ol>
     *   <li>Sets the system look and feel for native OS appearance</li>
     *   <li>Launches the splash screen on the Event Dispatch Thread (EDT)</li>
     * </ol>
     * </p>
     * 
     * @param args Command line arguments (not currently used)
     */
    public static void main(String[] args) {
        try {
            // Set system look and feel for better integration with the host OS
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run the application on EDT to ensure thread safety for Swing components
        SwingUtilities.invokeLater(() -> {
            new SplashScreenUI().setVisible(true);
        });
    }
}