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
    private final SplashScreenUI splashScreen;

    public ProfileManagementUI(SplashScreenUI splashScreen) {
        this.controller = new ProfileController();
        this.splashScreen = splashScreen;

        setTitle("Profile Management");
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
                    weightField.getText(),
                    (String) unitComboBox.getSelectedItem()
            );
            if (profile != null) {
                this.dispose();
                splashScreen.launchMainApplication(profile);
            }
        });
    }
}