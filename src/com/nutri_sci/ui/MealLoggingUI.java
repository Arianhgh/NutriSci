package com.nutri_sci.ui;

import com.nutri_sci.controller.MealController;
import com.nutri_sci.model.UserProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Date;

public class MealLoggingUI extends JFrame {
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final JComboBox<String> mealTypeComboBox = new JComboBox<>(new String[]{"Breakfast", "Lunch", "Dinner", "Snack"});
    private final JTextArea ingredientsArea = new JTextArea(8, 30);
    private final MealController controller;
    private final UserProfile userProfile;

    public MealLoggingUI(UserProfile userProfile) {
        this.controller = new MealController();
        this.userProfile = userProfile;

        setTitle("Log a Meal for " + userProfile.getName());
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Date:"));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        topPanel.add(dateSpinner);
        topPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacer
        topPanel.add(new JLabel("Select Meal Type:"));
        topPanel.add(mealTypeComboBox);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel ingredientsPanel = new JPanel(new BorderLayout());
        ingredientsPanel.setBorder(new TitledBorder("Enter Ingredients (one per line, e.g., '100g chicken')"));
        ingredientsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ingredientsPanel.add(new JScrollPane(ingredientsArea), BorderLayout.CENTER);
        mainPanel.add(ingredientsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton logMealButton = new JButton("Log This Meal");
        logMealButton.setFont(new Font("Arial", Font.BOLD, 14));
        logMealButton.setPreferredSize(new Dimension(150, 40));
        buttonPanel.add(logMealButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        logMealButton.addActionListener(e -> {
            boolean success = controller.logMeal(
                    userProfile,
                    (Date) dateSpinner.getValue(),
                    (String) mealTypeComboBox.getSelectedItem(),
                    ingredientsArea.getText()
            );
            if (success) {
                this.dispose();
            }
        });
    }
    public static void main(String[] args) {
        // Create a sample UserProfile for testing.
        UserProfile testProfile = new UserProfile();
        testProfile.setId(1); // Assuming a user with ID 1 exists or for testing purposes
        testProfile.setName("Test User");

        // Run the UI on the edt
        SwingUtilities.invokeLater(() -> {
            MealLoggingUI mealLogUI = new MealLoggingUI(testProfile);
            mealLogUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the app when this frame is closed
            mealLogUI.setVisible(true);
        });
    }
}