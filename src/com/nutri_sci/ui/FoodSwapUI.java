package com.nutri_sci.ui;

import com.nutri_sci.controller.SwapController;
import com.nutri_sci.model.Goal;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.SwapSuggestion;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.SwapEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FoodSwapUI extends JFrame {

    private final Meal originalMeal;
    private final UserProfile userProfile;
    private final SwapEngine swapEngine;
    private final SwapController swapController;

    // --- UI Components ---
    private final JComboBox<String> ingredientsToSwapBox;
    private final JComboBox<String> nutrientGoalBox = new JComboBox<>(new String[]{"Calories", "Protein", "Fiber"});
    private final JComboBox<String> goalTypeBox = new JComboBox<>(new String[]{"Decrease", "Increase"});
    private final JComboBox<String> goalIntensityBox = new JComboBox<>(new String[]{"by Percentage (%)", "by Absolute Amount (g/kcal)"});
    private final JTextField goalAmountField = new JTextField("10", 5);
    private final JSlider toleranceSlider = new JSlider(0, 50, 15);
    private final JList<SwapSuggestion> suggestedSwapsList = new JList<>();
    private final JCheckBox sameGroupOnlyCheckbox = new JCheckBox("Only suggest from same food group");
    private final JCheckBox strictToleranceCheckbox = new JCheckBox("Strictly enforce nutrient tolerance");

    // --- NEW: Components for the second goal ---
    private final JCheckBox enableSecondGoalCheckbox = new JCheckBox("Add a Second Goal");
    private final JComboBox<String> nutrientGoalBox2 = new JComboBox<>(new String[]{"Calories", "Protein", "Fiber"});
    private final JComboBox<String> goalTypeBox2 = new JComboBox<>(new String[]{"Decrease", "Increase"});
    private final JComboBox<String> goalIntensityBox2 = new JComboBox<>(new String[]{"by Percentage (%)", "by Absolute Amount (g/kcal)"});
    private final JTextField goalAmountField2 = new JTextField("5", 5);


    public FoodSwapUI(UserProfile userProfile, Meal mealToSwap) {
        this.userProfile = userProfile;
        this.originalMeal = mealToSwap;
        this.swapEngine = new SwapEngine();
        this.swapController = new SwapController();

        setTitle("Suggest Food Swaps");
        setSize(700, 750); // Increased height for new components
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Original Meal:"));
        selectionPanel.add(new JLabel(originalMeal.getMealType() + " (" + String.format("%.2f", originalMeal.getEstimatedCalories()) + " kcal)"));
        String[] ingredients = originalMeal.getIngredients().split("\n");
        ingredientsToSwapBox = new JComboBox<>(ingredients);
        selectionPanel.add(new JLabel("Swap this item:"));
        selectionPanel.add(ingredientsToSwapBox);

        JPanel goalDefinitionPanel = new JPanel(new GridBagLayout());
        goalDefinitionPanel.setBorder(new TitledBorder("Define Your Nutritional Goal(s)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // --- Row 0: Goal 1 ---
        gbc.gridx = 0; gbc.gridy = 0; goalDefinitionPanel.add(new JLabel("Goal 1:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; goalDefinitionPanel.add(goalTypeBox, gbc);
        gbc.gridx = 2; gbc.gridy = 0; goalDefinitionPanel.add(nutrientGoalBox, gbc);
        gbc.gridx = 3; gbc.gridy = 0; goalDefinitionPanel.add(goalIntensityBox, gbc);
        gbc.gridx = 4; gbc.gridy = 0; goalDefinitionPanel.add(goalAmountField, gbc);

        // --- Row 1: Second Goal Checkbox ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 5;
        goalDefinitionPanel.add(enableSecondGoalCheckbox, gbc);
        enableSecondGoalCheckbox.addActionListener(e -> toggleSecondGoal());

        // --- Row 2: Goal 2 (Initially hidden) ---
        gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.gridx = 0; goalDefinitionPanel.add(new JLabel("Goal 2:"), gbc);
        gbc.gridx = 1; goalDefinitionPanel.add(goalTypeBox2, gbc);
        gbc.gridx = 2; goalDefinitionPanel.add(nutrientGoalBox2, gbc);
        gbc.gridx = 3; goalDefinitionPanel.add(goalIntensityBox2, gbc);
        gbc.gridx = 4; goalDefinitionPanel.add(goalAmountField2, gbc);

        // --- Row 3: Tolerance Slider ---
        gbc.gridy = 3;
        gbc.gridx = 0; goalDefinitionPanel.add(new JLabel("Keep other nutrients within:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        toleranceSlider.setMajorTickSpacing(10);
        toleranceSlider.setMinorTickSpacing(5);
        toleranceSlider.setPaintTicks(true);
        toleranceSlider.setPaintLabels(true);
        goalDefinitionPanel.add(toleranceSlider, gbc);
        gbc.gridx = 4; gbc.gridy = 3; goalDefinitionPanel.add(new JLabel("%"), gbc);

        // --- Row 4: Checkboxes ---
        gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.gridx = 0; goalDefinitionPanel.add(sameGroupOnlyCheckbox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 3; goalDefinitionPanel.add(strictToleranceCheckbox, gbc);

        // --- Row 5: Find Button ---
        gbc.gridy = 5;
        JButton findSwapsButton = new JButton("Find Swaps");
        gbc.gridx = 0; gbc.gridwidth = 5; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 5, 5, 5); // Add top margin
        goalDefinitionPanel.add(findSwapsButton, gbc);

        JPanel suggestionsPanel = new JPanel(new BorderLayout());
        suggestionsPanel.setBorder(new TitledBorder("Suggested Swaps (Best matches on top)"));
        suggestedSwapsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionsPanel.add(new JScrollPane(suggestedSwapsList), BorderLayout.CENTER);

        JPanel swapExecutionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton performSwapButton = new JButton("Perform Swap with Selected Item");
        swapExecutionPanel.add(performSwapButton);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(selectionPanel, BorderLayout.NORTH);
        topContainer.add(goalDefinitionPanel, BorderLayout.CENTER);

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(suggestionsPanel, BorderLayout.CENTER);
        mainPanel.add(swapExecutionPanel, BorderLayout.SOUTH);

        add(mainPanel);

        findSwapsButton.addActionListener(e -> findAndDisplaySwaps());
        performSwapButton.addActionListener(e -> finalizeSwap());

        toggleSecondGoal(); // Set initial state
    }

    /**
     * Toggles the visibility and enabled state of the second goal's components.
     */
    private void toggleSecondGoal() {
        boolean enabled = enableSecondGoalCheckbox.isSelected();
        nutrientGoalBox2.setEnabled(enabled);
        goalTypeBox2.setEnabled(enabled);
        goalIntensityBox2.setEnabled(enabled);
        goalAmountField2.setEnabled(enabled);
    }

    private void findAndDisplaySwaps() {
        String itemToSwap = (String) ingredientsToSwapBox.getSelectedItem();
        double tolerance = toleranceSlider.getValue();
        boolean sameGroupOnly = sameGroupOnlyCheckbox.isSelected();
        boolean strictTolerance = strictToleranceCheckbox.isSelected();

        List<Goal> goals = new ArrayList<>();
        try {
            // Goal 1
            goals.add(new Goal(
                    (String) nutrientGoalBox.getSelectedItem(),
                    (String) goalTypeBox.getSelectedItem(),
                    Double.parseDouble(goalAmountField.getText()),
                    ((String) goalIntensityBox.getSelectedItem()).contains("Percentage")
            ));

            // Goal 2 (if enabled)
            if (enableSecondGoalCheckbox.isSelected()) {
                goals.add(new Goal(
                        (String) nutrientGoalBox2.getSelectedItem(),
                        (String) goalTypeBox2.getSelectedItem(),
                        Double.parseDouble(goalAmountField2.getText()),
                        ((String) goalIntensityBox2.getSelectedItem()).contains("Percentage")
                ));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for the goal amounts.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (itemToSwap == null) return;

        List<SwapSuggestion> suggestions = swapEngine.findSwaps(originalMeal, itemToSwap, goals, tolerance, sameGroupOnly, strictTolerance);

        if (suggestions.isEmpty()) {
            DefaultListModel<SwapSuggestion> model = new DefaultListModel<>();
            model.addElement(new SwapSuggestion("No suitable swaps found matching your criteria.", null, 0, null, null){
                @Override
                public String toString() { return getFoodName(); }
            });
            suggestedSwapsList.setModel(model);
        } else {
            DefaultListModel<SwapSuggestion> model = new DefaultListModel<>();
            suggestions.forEach(model::addElement);
            suggestedSwapsList.setModel(model);
        }
    }

    private void finalizeSwap() {
        String itemToSwap = (String) ingredientsToSwapBox.getSelectedItem();
        SwapSuggestion selectedSuggestion = suggestedSwapsList.getSelectedValue();

        if (itemToSwap == null || selectedSuggestion == null || selectedSuggestion.getFoodName().startsWith("No suitable swaps")) {
            JOptionPane.showMessageDialog(this, "Please find and select a valid swap from the list.", "Swap Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newItem = selectedSuggestion.getFoodName();
        Meal newSwappedMeal = swapController.performAndSaveSwap(userProfile, originalMeal, itemToSwap, newItem);

        if (newSwappedMeal != null) {
            this.dispose();
            int response = JOptionPane.showConfirmDialog(null,
                    "Would you like to apply this swap to previously recorded meals?",
                    "Apply Swap Over Time",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                new ApplySwapOverTimeUI(userProfile, itemToSwap, newItem).setVisible(true);
            }
        }
    }
}