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
    private final JComboBox<String> unitComboBox = new JComboBox<>(new String[]{"Metric (cm, kg)", "Imperial (in, lbs)"});

    private final ProfileController controller;
    private UserProfile userProfile; // Can be null if creating a new profile
    private final MainApplicationUI mainAppUI; // To refresh the welcome label

    public ProfileManagementUI(SplashScreenUI splashScreen) {
        this(null, null, splashScreen);
    }

    public ProfileManagementUI(UserProfile userProfile, MainApplicationUI mainAppUI) {
        this(userProfile, mainAppUI, null);
    }

    private ProfileManagementUI(UserProfile userProfile, MainApplicationUI mainAppUI, SplashScreenUI splashScreen) {
        this.controller = new ProfileController();
        this.userProfile = userProfile;
        this.mainAppUI = mainAppUI;

        setTitle(userProfile == null ? "Create New Profile" : "Edit Profile");
        setSize(450, 400);
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
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Height:"), gbc);
        gbc.gridx = 1; panel.add(heightField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Weight:"), gbc);
        gbc.gridx = 1; panel.add(weightField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Units:"), gbc);
        gbc.gridx = 1; panel.add(unitComboBox, gbc);

        gbc.gridx = 1; gbc.gridy = 6; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        JButton saveButton = new JButton(userProfile == null ? "Create and Login" : "Save Changes");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(saveButton, gbc);

        add(panel);

        if (userProfile != null) {
            populateFields();
        }

        saveButton.addActionListener(e -> {
            if (userProfile == null) {
                UserProfile newProfile = controller.createProfile(
                        nameField.getText(),
                        (String) sexComboBox.getSelectedItem(),
                        (Date) dobSpinner.getValue(),
                        heightField.getText(),
                        weightField.getText(),
                        (String) unitComboBox.getSelectedItem()
                );
                if (newProfile != null) {
                    this.dispose();
                    splashScreen.launchMainApplication(newProfile);
                }
            } else {
                boolean success = controller.updateProfile(
                        userProfile,
                        nameField.getText(),
                        (String) sexComboBox.getSelectedItem(),
                        (Date) dobSpinner.getValue(),
                        heightField.getText(),
                        weightField.getText(),
                        (String) unitComboBox.getSelectedItem()
                );
                if (success) {
                    mainAppUI.refreshWelcomeLabel();
                    this.dispose();
                }
            }
        });
    }

    private void populateFields() {
        nameField.setText(userProfile.getName());
        sexComboBox.setSelectedItem(userProfile.getSex());
        dobSpinner.setValue(userProfile.getDateOfBirth());
        heightField.setText(String.valueOf(userProfile.getHeight()));
        weightField.setText(String.valueOf(userProfile.getWeight()));
        unitComboBox.setSelectedItem(userProfile.getMeasurementUnit());
    }
}