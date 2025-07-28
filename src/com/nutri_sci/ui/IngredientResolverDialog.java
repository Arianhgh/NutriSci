package com.nutri_sci.ui;

import com.nutri_sci.model.FoodItem;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog for resolving ambiguous ingredient names to specific food items.
 * <p>
 * This dialog is presented to users when an ingredient they've entered matches
 * multiple items in the nutrition database. It allows users to select the
 * specific food item they intended from a list of suggestions, ensuring accurate
 * nutritional calculations and meal logging.
 * </p>
 * <p>
 * The dialog features:
 * <ul>
 *   <li>Clear presentation of the original user query</li>
 *   <li>List of matching food items from the database</li>
 *   <li>Simple selection interface with confirmation</li>
 *   <li>Modal operation to ensure resolution before proceeding</li>
 * </ul>
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.model.FoodItem
 * @see com.nutri_sci.controller.MealController
 */
public class IngredientResolverDialog extends JDialog {
    
    /** List component displaying available food item suggestions */
    private JList<FoodItem> suggestionList;
    
    /** The food item selected by the user, or null if none selected */
    private FoodItem selectedFoodItem = null;

    /**
     * Constructs a new ingredient resolver dialog.
     * <p>
     * Creates a modal dialog that presents the user with a list of food items
     * matching their ingredient query. The dialog is automatically sized and
     * centered relative to the parent window.
     * </p>
     * 
     * @param owner the parent frame for modal behavior and positioning
     * @param originalQuery the original ingredient text entered by the user
     * @param suggestions list of FoodItem objects that match the user's query
     */
    public IngredientResolverDialog(Frame owner, String originalQuery, List<FoodItem> suggestions) {
        super(owner, "Confirm Ingredient: " + originalQuery, true);
        setSize(500, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Set up the suggestion list
        DefaultListModel<FoodItem> model = new DefaultListModel<>();
        suggestions.forEach(model::addElement);
        suggestionList = new JList<>(model);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setSelectedIndex(0); // Pre-select the first option

        // Set up the selection button
        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> {
            selectedFoodItem = suggestionList.getSelectedValue();
            dispose();
        });

        // Layout the dialog components
        add(new JScrollPane(suggestionList), BorderLayout.CENTER);
        add(selectButton, BorderLayout.SOUTH);
    }

    /**
     * Displays the dialog and returns the user's food item selection.
     * <p>
     * This method shows the modal dialog and blocks until the user makes a
     * selection or closes the dialog. The selected food item is returned,
     * or null if the dialog was dismissed without a selection.
     * </p>
     * 
     * @return the FoodItem selected by the user, or null if no selection was made
     */
    public FoodItem showDialog() {
        setVisible(true);
        return selectedFoodItem;
    }
}