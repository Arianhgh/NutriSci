package com.nutri_sci.ui;

import com.nutri_sci.controller.ProfileController;
import com.nutri_sci.model.UserProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;

public class ProfileManagementUI extends JFrame {
    private final JTextField nameField = new JTextField(20);
    private final JComboBox<String> sexComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JSpinner dobSpinner = new JSpinner(new SpinnerDateModel());
    private final JTextField heightField = new JTextField(20);
    private final JTextField weightField = new JTextField(20);

    private final ProfileController controller;
    private final SplashScreenUI splashScreen;

    public ProfileManagementUI(SplashScreenUI splashScreen) {
        this.controller = new ProfileController();
        this.splashScreen = splashScreen;

        setTitle("Profile Management");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Sex:"), gbc);
        gbc.gridx = 1; panel.add(sexComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1; JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dobSpinner, "yyyy-MM-dd");
        dobSpinner.setEditor(dateEditor);
        panel.add(dobSpinner, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Height (cm):"), gbc);
        gbc.gridx = 1; panel.add(heightField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Weight (kg):"), gbc);
        gbc.gridx = 1; panel.add(weightField, gbc);

        gbc.gridx = 1; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        JButton saveButton = new JButton("Create and Login");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(saveButton, gbc);

        add(panel);

        saveButton.addActionListener(e -> {
            UserProfile profile = controller.createProfile(
                    nameField.getText(),
                    (String) sexComboBox.getSelectedItem(),
                    (Date) dobSpinner.getValue(),
                    heightField.getText(),
                    weightField.getText()
            );
            if (profile != null) {
                this.dispose();
                splashScreen.launchMainApplication(profile); // Launch main app
            }
        });
    }
    public static void main(String[] args) {
        // Run the UI on the edt
        SwingUtilities.invokeLater(() -> {
            // Create a dummy SplashScreenUI for testing purposes.
            SplashScreenUI mockSplashScreen = new SplashScreenUI() {
                @Override
                public void launchMainApplication(UserProfile profile) {
                    System.out.println("Mock Splash Screen: Profile created for " + profile.getName() + ". In a real run, the main app would launch.");
                    System.exit(0);
                }
            };
            mockSplashScreen.setVisible(false); // We don't need to see the actual splash screen.

            ProfileManagementUI profileUI = new ProfileManagementUI(mockSplashScreen);
            profileUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the app when this frame is closed
            profileUI.setVisible(true);
        });
    }
}
