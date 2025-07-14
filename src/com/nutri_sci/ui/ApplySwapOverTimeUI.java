package com.nutri_sci.ui;

import com.nutri_sci.controller.SwapController;
import com.nutri_sci.model.UserProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;

public class ApplySwapOverTimeUI extends JFrame {

    private final UserProfile userProfile;
    private final String itemToSwap;
    private final String newItem;
    private final SwapController swapController;

    private final JSpinner startDateSpinner;
    private final JSpinner endDateSpinner;
    private final JCheckBox allTimeCheckBox;

    public ApplySwapOverTimeUI(UserProfile userProfile, String itemToSwap, String newItem) {
        this.userProfile = userProfile;
        this.itemToSwap = itemToSwap;
        this.newItem = newItem;
        this.swapController = new SwapController();

        setTitle("Apply Swap Over Time");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel dateSelectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startDateEditor);

        endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endDateEditor);

        allTimeCheckBox = new JCheckBox("Apply to all recorded meals");
        allTimeCheckBox.addActionListener(e -> toggleDateSpinners());

        gbc.gridx = 0;
        gbc.gridy = 0;
        dateSelectionPanel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1;
        dateSelectionPanel.add(startDateSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        dateSelectionPanel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 1;
        dateSelectionPanel.add(endDateSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        dateSelectionPanel.add(allTimeCheckBox, gbc);

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applySwap());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        dateSelectionPanel.add(applyButton, gbc);

        mainPanel.add(dateSelectionPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void toggleDateSpinners() {
        boolean enabled = !allTimeCheckBox.isSelected();
        startDateSpinner.setEnabled(enabled);
        endDateSpinner.setEnabled(enabled);
    }

    private void applySwap() {
        Date startDate = allTimeCheckBox.isSelected() ? null : (Date) startDateSpinner.getValue();
        Date endDate = allTimeCheckBox.isSelected() ? null : (Date) endDateSpinner.getValue();

        if (startDate != null && endDate != null && startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after the end date.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        swapController.applySwapOverTime(userProfile, itemToSwap, newItem, startDate, endDate);
        this.dispose();
    }
}