package com.nutri_sci.ui;

import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.UserProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Initial welcome screen for the NutriSci application.
 * <p>
 * The SplashScreenUI serves as the entry point for user interaction, providing
 * options to either create a new user profile or load an existing one. This
 * screen establishes the application's visual identity and guides users through
 * the initial setup process.
 * </p>
 * <p>
 * Features include:
 * <ul>
 *   <li>Branded welcome screen with application logo and title</li>
 *   <li>Profile creation workflow initiation</li>
 *   <li>Existing profile selection and loading</li>
 *   <li>Seamless transition to the main application interface</li>
 * </ul>
 * </p>
 * 
 * @author NutriSci Development Team
 * @version 1.0
 * @since 1.0
 * @see ProfileManagementUI
 * @see MainApplicationUI
 */
public class SplashScreenUI extends JFrame {

    /**
     * Constructs and initializes the splash screen interface.
     * <p>
     * Sets up the main welcome screen with branding elements and navigation
     * buttons. Configures event handlers for profile creation and loading
     * operations, establishing the user's entry path into the application.
     * </p>
     */
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

        // Set up event handlers for navigation
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

    /**
     * Launches the main application interface with the specified user profile.
     * <p>
     * This method transitions from the splash screen to the main application
     * interface, passing the selected or created user profile to establish
     * the user context for the application session.
     * </p>
     * 
     * @param profile the user profile to use for the application session
     */
    public void launchMainApplication(UserProfile profile) {
        if (profile != null) {
            MainApplicationUI mainApp = new MainApplicationUI(profile);
            mainApp.setVisible(true);
            this.dispose();
        }
    }

    /**
     * Applies consistent styling to navigation buttons.
     * <p>
     * This utility method ensures consistent visual presentation across
     * all buttons in the splash screen interface, maintaining the
     * application's design standards.
     * </p>
     * 
     * @param button the button to apply styling to
     */
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 40));
    }
}