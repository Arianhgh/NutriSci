package com.nutri_sci.ui;

import com.nutri_sci.model.Meal;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.MealDataNotifier;
import com.nutri_sci.database.DBManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MainApplicationUI extends JFrame implements Observer {

    private final UserProfile userProfile;
    private final DBManager dbManager;
    private JTable mealLogTable;
    private DefaultTableModel tableModel;

    public MainApplicationUI(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.dbManager = DBManager.getInstance();

        MealDataNotifier.getInstance().addObserver(this);

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
        JLabel welcomeLabel = new JLabel("Welcome, " + userProfile.getName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(welcomeLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel mealLogPanel = new JPanel(new BorderLayout());
        mealLogPanel.setBorder(BorderFactory.createTitledBorder("Your Meal Journal"));

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
        JButton swapFoodButton = new JButton("Swap Food Item");
        JButton visualizeButton = new JButton("Visualize Data");

        styleControlButton(logMealButton);
        styleControlButton(swapFoodButton);
        styleControlButton(visualizeButton);

        controlPanel.add(logMealButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(swapFoodButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(visualizeButton);

        mainPanel.add(controlPanel, BorderLayout.EAST);

        add(mainPanel);

        logMealButton.addActionListener(e -> new MealLoggingUI(userProfile).setVisible(true));

        swapFoodButton.addActionListener(e -> {
            int selectedRow = mealLogTable.getSelectedRow();
            if (selectedRow >= 0) {
                Meal selectedMeal = dbManager.getMealsForUser(userProfile.getId()).get(selectedRow);
                // ***FIXED***: Pass the userProfile to the FoodSwapUI.
                new FoodSwapUI(userProfile, selectedMeal).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a meal from the journal to perform a swap.", "No Meal Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        visualizeButton.addActionListener(e -> new VisualizationUI().setVisible(true));
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
                    meal.getMealType(),
                    meal.getIngredients().replace("\n", ", "),
                    String.format("%.2f", meal.getEstimatedCalories())
            };
            tableModel.addRow(row);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        refreshMealTable();
    }
}