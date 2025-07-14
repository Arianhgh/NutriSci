package com.nutri_sci.ui;

import com.nutri_sci.model.Meal;
import com.nutri_sci.service.NutrientCalculator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SwapComparisonUI extends JFrame {

    public SwapComparisonUI(Meal originalMeal, Meal swappedMeal) {
        setTitle("Swap Comparison");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ensure nutrient data is available
        if (originalMeal.getNutrientBreakdown() == null) {
            originalMeal.setNutrientBreakdown(new NutrientCalculator().calculateNutrientsForMeal(originalMeal.getIngredients()));
        }
        if (swappedMeal.getNutrientBreakdown() == null) {
            swappedMeal.setNutrientBreakdown(new NutrientCalculator().calculateNutrientsForMeal(swappedMeal.getIngredients()));
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createMealPanel("Original Meal", originalMeal),
                createMealPanel("Swapped Meal", swappedMeal));
        splitPane.setResizeWeight(0.5);
        add(splitPane);
    }

    private JPanel createMealPanel(String title, Meal meal) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder(title));

        JTextArea ingredientsArea = new JTextArea(10, 30);
        ingredientsArea.setEditable(false);
        ingredientsArea.setText(meal.getIngredients());
        panel.add(new JScrollPane(ingredientsArea), BorderLayout.NORTH);

        JTextArea nutrientsArea = new JTextArea(15, 30);
        nutrientsArea.setEditable(false);
        nutrientsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        Map<String, Double> nutrients = meal.getNutrientBreakdown();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
            sb.append(String.format("%-15s: %.2f\n", entry.getKey(), entry.getValue()));
        }
        nutrientsArea.setText(sb.toString());
        panel.add(new JScrollPane(nutrientsArea), BorderLayout.CENTER);

        return panel;
    }
}