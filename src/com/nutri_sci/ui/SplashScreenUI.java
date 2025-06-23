package com.nutri_sci.ui;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.UserProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SplashScreenUI extends JFrame {

    public SplashScreenUI() {
        setTitle("Welcome to NutriSci: SwEATch to better!");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("NutriSci", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(new Color(60, 179, 113));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(Color.WHITE);
        JButton createProfileButton = new JButton("Create New Profile");
        JButton loadProfileButton = new JButton("Load Existing Profile");
        styleButton(createProfileButton);
        styleButton(loadProfileButton);
        buttonPanel.add(createProfileButton);
        buttonPanel.add(loadProfileButton);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        add(mainPanel);

        createProfileButton.addActionListener(e -> {
            ProfileManagementUI profileUI = new ProfileManagementUI(this);
            profileUI.setVisible(true);
            this.setVisible(false);
        });

        loadProfileButton.addActionListener(e -> {
            Object[] users = DBManager.getInstance().getAllUserNames().toArray();
            if (users.length == 0) {
                JOptionPane.showMessageDialog(this, "No profiles found. Please create one.", "Load Profile", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String selectedUser = (String) JOptionPane.showInputDialog(this, "Select a profile to load:",
                    "Load Profile", JOptionPane.PLAIN_MESSAGE, null, users, users[0]);

            if (selectedUser != null) {
                UserProfile profile = DBManager.getInstance().getProfile(selectedUser);
                launchMainApplication(profile);
            }
        });
    }

    public void launchMainApplication(UserProfile profile) {
        if (profile != null) {
            MainApplicationUI mainApp = new MainApplicationUI(profile);
            mainApp.setVisible(true);
            this.dispose();
        }
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 40));
    }
}