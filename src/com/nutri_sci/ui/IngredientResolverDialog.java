package com.nutri_sci.ui;

import com.nutri_sci.model.FoodItem;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A modal dialog that prompts the user to select the correct food item
 * from a list of suggestions.
 */
public class IngredientResolverDialog extends JDialog {
    private JList<FoodItem> suggestionList;
    private FoodItem selectedFoodItem = null;

    public IngredientResolverDialog(Frame owner, String originalQuery, List<FoodItem> suggestions) {
        super(owner, "Confirm Ingredient: " + originalQuery, true);
        setSize(500, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        DefaultListModel<FoodItem> model = new DefaultListModel<>();
        suggestions.forEach(model::addElement);
        suggestionList = new JList<>(model);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setSelectedIndex(0);

        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> {
            selectedFoodItem = suggestionList.getSelectedValue();
            dispose();
        });

        add(new JScrollPane(suggestionList), BorderLayout.CENTER);
        add(selectButton, BorderLayout.SOUTH);
    }

    /**
     * Shows the dialog and returns the FoodItem chosen by the user.
     * @return The selected FoodItem, or null if the dialog was closed without a selection.
     */
    public FoodItem showDialog() {
        setVisible(true);
        return selectedFoodItem;
    }
}