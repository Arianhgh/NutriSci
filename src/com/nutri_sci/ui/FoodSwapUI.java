package com.nutri_sci.ui;

import com.nutri_sci.controller.SwapController;
import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.SwapEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class FoodSwapUI extends JFrame {

    private final Meal originalMeal;
    private final UserProfile userProfile;
    private final SwapEngine swapEngine;
    private final SwapController swapController;

    private final JComboBox<String> ingredientsToSwapBox;
    private final JComboBox<String> nutrientGoalBox = new JComboBox<>(new String[]{"Calories", "Protein", "Fiber"});
    private final JComboBox<String> goalTypeBox = new JComboBox<>(new String[]{"Increase", "Decrease"});
    private final JComboBox<String> goalIntensityBox = new JComboBox<>(new String[]{"by a normal amount", "by a significant amount"});
    private final JList<String> suggestedSwapsList = new JList<>();

    public FoodSwapUI(UserProfile userProfile, Meal mealToSwap) {
        this.userProfile = userProfile;
        this.originalMeal = mealToSwap;
        this.swapEngine = new SwapEngine();
        this.swapController = new SwapController();

        setTitle("Suggest Food Swaps");
        setSize(600, 500);
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

        JPanel goalPanel = new JPanel();
        goalPanel.setBorder(new TitledBorder("Define Your Nutritional Goal"));
        goalPanel.add(new JLabel("Goal:"));
        goalPanel.add(goalTypeBox);
        goalPanel.add(nutrientGoalBox);
        goalPanel.add(goalIntensityBox);
        JButton findSwapsButton = new JButton("Find Swaps");
        goalPanel.add(findSwapsButton);

        JPanel suggestionsPanel = new JPanel(new BorderLayout());
        suggestionsPanel.setBorder(new TitledBorder("Suggested Swaps"));
        suggestedSwapsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionsPanel.add(new JScrollPane(suggestedSwapsList), BorderLayout.CENTER);

        JPanel swapExecutionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton performSwapButton = new JButton("Perform Swap with Selected Item");
        swapExecutionPanel.add(performSwapButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(selectionPanel, BorderLayout.NORTH);
        topPanel.add(goalPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(suggestionsPanel, BorderLayout.CENTER);
        mainPanel.add(swapExecutionPanel, BorderLayout.SOUTH);

        add(mainPanel);

        findSwapsButton.addActionListener(e -> findAndDisplaySwaps());
        performSwapButton.addActionListener(e -> finalizeSwap());
    }

    private void findAndDisplaySwaps() {
        String itemToSwap = (String) ingredientsToSwapBox.getSelectedItem();
        String nutrient = (String) nutrientGoalBox.getSelectedItem();
        String goalType = (String) goalTypeBox.getSelectedItem();
        String intensity = (String) goalIntensityBox.getSelectedItem();

        if (itemToSwap == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to swap.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> suggestions = swapEngine.findSwaps(originalMeal, itemToSwap, nutrient, goalType, intensity);

        if (suggestions.isEmpty()) {
            suggestedSwapsList.setListData(new String[]{"No suitable swaps found matching your criteria."});
        } else {
            suggestedSwapsList.setListData(suggestions.toArray(new String[0]));
        }
    }

    /**
     * This method now orchestrates the final swap and save operation,
     * handling the new "(same food group)" label.
     */
    private void finalizeSwap() {
        String itemToSwap = (String) ingredientsToSwapBox.getSelectedItem();
        String selectedSuggestion = suggestedSwapsList.getSelectedValue();

        if (itemToSwap == null || selectedSuggestion == null || selectedSuggestion.startsWith("No suitable swaps")) {
            JOptionPane.showMessageDialog(this, "Please find and select a valid swap from the list.", "Swap Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Remove the "(same food group)" suffix before processing.
        String newItem = selectedSuggestion.replace(" (same food group)", "").trim();

        // Use the controller to perform the swap and save it to the database.
        Meal newSwappedMeal = swapController.performAndSaveSwap(userProfile, originalMeal, itemToSwap, newItem);

        // If the swap was successful, close the window. The main UI will update automatically.
        if (newSwappedMeal != null) {
            this.dispose();
        }
    }
    public static void main(String[] args) {
        // 1. Create a sample UserProfile for testing.
        UserProfile testProfile = new UserProfile();
        testProfile.setId(1);
        testProfile.setName("Test User");

        // 2. Create a sample Meal object to be swapped.
        Meal mealToSwap = new Meal();
        mealToSwap.setMealId(10); // A sample meal ID
        mealToSwap.setMealType("Lunch");
        mealToSwap.setEstimatedCalories(550.0);
        mealToSwap.setIngredients("150g chicken breast\n100g white rice\n50g broccoli");

        // 3. Run the UI on the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            FoodSwapUI foodSwapUI = new FoodSwapUI(testProfile, mealToSwap);
            foodSwapUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the app when this frame is closed
            foodSwapUI.setVisible(true);
        });
    }
}