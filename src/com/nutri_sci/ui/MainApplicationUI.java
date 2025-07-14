package com.nutri_sci.ui;

import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.MealDataNotifier;
import com.nutri_sci.database.DBManager;
import com.nutri_sci.service.NutrientCalculator; // <-- IMPORT ADDED

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class MainApplicationUI extends JFrame implements PropertyChangeListener {

    private final UserProfile userProfile;
    private final DBManager dbManager;
    private JTable mealLogTable;
    private DefaultTableModel tableModel;
    private JLabel welcomeLabel;

    public MainApplicationUI(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.dbManager = DBManager.getInstance();

        // Register as a listener for property changes
        MealDataNotifier.getInstance().addPropertyChangeListener(this);

        setTitle("NutriSci Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        refreshMealTable();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomeLabel = new JLabel("Welcome, " + userProfile.getName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(welcomeLabel);

        JButton editProfileButton = new JButton("Edit Profile");
        headerPanel.add(editProfileButton);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel mealLogPanel = new JPanel(new BorderLayout());
        mealLogPanel.setBorder(BorderFactory.createTitledBorder("Your Meal Journal (Double-click a meal to see details)"));

        String[] columnNames = {"Date", "Meal Type", "Ingredients", "Est. Calories"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        mealLogTable = new JTable(tableModel);
        mealLogTable.setFillsViewportHeight(true);
        mealLogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(mealLogTable);
        mealLogPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(mealLogPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton logMealButton = new JButton("Log New Meal");
        JButton swapFoodButton = new JButton("Suggest a Swap");
        JButton compareSwapButton = new JButton("Compare Swapped Meal");
        JButton visualizeButton = new JButton("Visualize Data");

        styleControlButton(logMealButton);
        styleControlButton(swapFoodButton);
        styleControlButton(compareSwapButton);
        styleControlButton(visualizeButton);

        controlPanel.add(logMealButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(swapFoodButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(compareSwapButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(visualizeButton);

        mainPanel.add(controlPanel, BorderLayout.EAST);

        add(mainPanel);

        logMealButton.addActionListener(e -> new MealLoggingUI(userProfile).setVisible(true));

        swapFoodButton.addActionListener(e -> {
            int selectedRow = mealLogTable.getSelectedRow();
            if (selectedRow >= 0) {
                Meal selectedMeal = dbManager.getMealsForUser(userProfile.getId()).get(selectedRow);
                new FoodSwapUI(userProfile, selectedMeal).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a meal from the journal to perform a swap.", "No Meal Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        compareSwapButton.addActionListener(e -> {
            int selectedRow = mealLogTable.getSelectedRow();
            if (selectedRow >= 0) {
                List<Meal> currentMeals = dbManager.getMealsForUser(userProfile.getId());
                Meal selectedMeal = currentMeals.get(selectedRow);

                if (selectedMeal.isSwapped() && selectedMeal.getOriginalMealId() != null) {
                    Meal originalMeal = dbManager.getMealById(selectedMeal.getOriginalMealId());
                    if (originalMeal != null) {
                        new SwapComparisonUI(originalMeal, selectedMeal).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Could not find the original meal to compare against.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a meal that has been swapped to make a comparison.", "Not a Swapped Meal", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a meal from the journal.", "No Meal Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        visualizeButton.addActionListener(e -> new VisualizationHubUI(userProfile).setVisible(true));

        editProfileButton.addActionListener(e -> {
            ProfileManagementUI profileUI = new ProfileManagementUI(new SplashScreenUI());
            profileUI.setVisible(true);
        });

        mealLogTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = mealLogTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        Meal selectedMeal = dbManager.getMealsForUser(userProfile.getId()).get(selectedRow);
                        displayNutrientBreakdown(selectedMeal);
                    }
                }
            }
        });
    }

    private void styleControlButton(JButton button) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    public void refreshMealTable() {
        tableModel.setRowCount(0);
        List<Meal> meals = dbManager.getMealsForUser(userProfile.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Meal meal : meals) {
            Object[] row = {
                    sdf.format(meal.getDate()),
                    meal.getMealType() + (meal.isSwapped() ? " (Swapped)" : ""),
                    meal.getIngredients().replace("\n", ", "),
                    String.format("%.2f", meal.getEstimatedCalories())
            };
            tableModel.addRow(row);
        }
    }

    private void displayNutrientBreakdown(Meal meal) {
        // Use the NutrientCalculator class to get the full breakdown
        NutrientCalculator calculator = new NutrientCalculator();
        Map<String, Double> nutrients = calculator.calculateNutrientsForMeal(meal.getIngredients());
        meal.setNutrientBreakdown(nutrients);

        if (nutrients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No detailed nutrient information could be calculated for this meal.", "Nutrient Breakdown", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Nutrient Breakdown for ").append(meal.getMealType()).append(":\n\n");
        for (Map.Entry<String, Double> entry : nutrients.entrySet()) {
            breakdown.append(String.format("%-15s: %.2f\n", entry.getKey(), entry.getValue()));
        }

        JTextArea textArea = new JTextArea(breakdown.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 400));
        JOptionPane.showMessageDialog(this, scrollPane, "Nutrient Breakdown", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Check if the change notification is about meal data
        if ("mealData".equals(evt.getPropertyName())) {
            SwingUtilities.invokeLater(this::refreshMealTable);
        }
    }
}