package com.nutri_sci;

import com.nutri_sci.ui.SplashScreenUI;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run the application on edt
        SwingUtilities.invokeLater(() -> {
            new SplashScreenUI().setVisible(true);
        });
    }
}