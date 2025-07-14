package com.nutri_sci.ui;

import com.nutri_sci.controller.VisualizationController;
import com.nutri_sci.database.DBManager;
import com.nutri_sci.model.UserProfile;
import com.nutri_sci.service.CanadaFoodGuideService;
import com.nutri_sci.service.ChartRenderer;
import com.nutri_sci.service.chart.BarChartFactory;
import com.nutri_sci.service.chart.LineChartFactory;
import com.nutri_sci.service.chart.PieChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Date;

public class VisualizationHubUI extends JFrame {

    private final UserProfile userProfile;
    private final VisualizationController controller;
    private final ChartRenderer chartRenderer;
    private final CanadaFoodGuideService cfgService; // Service for CFG data

    // Components for Daily Intake Tab
    private JSpinner intakeStartDateSpinner;
    private JSpinner intakeEndDateSpinner;
    private JPanel macroChartPanel; // Panel for the macronutrient chart
    private JPanel microChartPanel; // Panel for the micronutrient chart
    private JLabel rdaLabel;

    // Components for Swap Effect Tab
    private JSpinner swapStartDateSpinner;
    private JSpinner swapEndDateSpinner;
    private JComboBox<String> nutrientComboBox;
    private JComboBox<String> chartTypeComboBox;
    private JPanel swapChartPanel;

    // Components for CFG Alignment Tab
    private JSpinner cfgStartDateSpinner;
    private JSpinner cfgEndDateSpinner;
    private JPanel cfgChartPanel; // Main panel for the charts
    private JPanel userPlatePanel; // Panel for user's plate chart
    private JPanel cfgPlatePanel; // Panel for CFG recommended plate chart


    public VisualizationHubUI(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.controller = new VisualizationController(userProfile);
        this.chartRenderer = new ChartRenderer();
        this.cfgService = new CanadaFoodGuideService(); // Initialize the new service

        setTitle("NutriSci - Visualization Hub");
        setSize(1200, 700); // Increased width to accommodate two charts
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Daily Intake Analysis", createDailyIntakePanel());
        tabbedPane.addTab("Swap Effect Analysis", createSwapEffectPanel());
        tabbedPane.addTab("CFG Alignment", createCfgAlignmentPanel()); // Add new tab

        add(tabbedPane);


        setSmartDefaultDate();
    }

    private void setSmartDefaultDate() {
        Date mostRecentMealDate = DBManager.getInstance().getMostRecentMealDate(userProfile.getId());
        if (mostRecentMealDate != null) {
            intakeStartDateSpinner.setValue(mostRecentMealDate);
            intakeEndDateSpinner.setValue(mostRecentMealDate);
            swapStartDateSpinner.setValue(mostRecentMealDate);
            swapEndDateSpinner.setValue(mostRecentMealDate);
            cfgStartDateSpinner.setValue(mostRecentMealDate); // Set default for new tab
            cfgEndDateSpinner.setValue(mostRecentMealDate); // Set default for new tab
        }
    }

    private JPanel createDailyIntakePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        intakeStartDateSpinner = new JSpinner(new SpinnerDateModel());
        intakeEndDateSpinner = new JSpinner(new SpinnerDateModel());
        intakeStartDateSpinner.setEditor(new JSpinner.DateEditor(intakeStartDateSpinner, "yyyy-MM-dd"));
        intakeEndDateSpinner.setEditor(new JSpinner.DateEditor(intakeEndDateSpinner, "yyyy-MM-dd"));

        controlsPanel.add(new JLabel("Start Date:"));
        controlsPanel.add(intakeStartDateSpinner);
        controlsPanel.add(new JLabel("End Date:"));
        controlsPanel.add(intakeEndDateSpinner);
        JButton generateButton = new JButton("Generate Report");
        controlsPanel.add(generateButton);
        panel.add(controlsPanel, BorderLayout.NORTH);

        // Display Area
        JPanel displayPanel = new JPanel(new BorderLayout(10, 10));

        // Container for the two pie charts
        JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 10, 10));
        macroChartPanel = new JPanel(new BorderLayout());
        microChartPanel = new JPanel(new BorderLayout());

        macroChartPanel.setBorder(new TitledBorder("Macronutrient Distribution"));
        microChartPanel.setBorder(new TitledBorder("Top 5 Other Nutrients"));

        macroChartPanel.add(new JLabel("Generate a report to see your macro breakdown.", SwingConstants.CENTER));
        microChartPanel.add(new JLabel("Generate a report to see your micro breakdown.", SwingConstants.CENTER));

        chartsContainer.add(macroChartPanel);
        chartsContainer.add(microChartPanel);

        displayPanel.add(chartsContainer, BorderLayout.CENTER);

        rdaLabel = new JLabel(" ", SwingConstants.CENTER);
        rdaLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        displayPanel.add(rdaLabel, BorderLayout.SOUTH);
        panel.add(displayPanel, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateDailyIntakeReport());

        return panel;
    }

    private JPanel createSwapEffectPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        swapStartDateSpinner = new JSpinner(new SpinnerDateModel());
        swapEndDateSpinner = new JSpinner(new SpinnerDateModel());
        swapStartDateSpinner.setEditor(new JSpinner.DateEditor(swapStartDateSpinner, "yyyy-MM-dd"));
        swapEndDateSpinner.setEditor(new JSpinner.DateEditor(swapEndDateSpinner, "yyyy-MM-dd"));

        nutrientComboBox = new JComboBox<>(new String[]{"Calories", "Protein", "Fiber"});
        chartTypeComboBox = new JComboBox<>(new String[]{"Bar Chart", "Line Chart"});
        controlsPanel.add(new JLabel("Start:"));
        controlsPanel.add(swapStartDateSpinner);
        controlsPanel.add(new JLabel("End:"));
        controlsPanel.add(swapEndDateSpinner);
        controlsPanel.add(new JLabel("Nutrient:"));
        controlsPanel.add(nutrientComboBox);
        controlsPanel.add(new JLabel("Chart Type:"));
        controlsPanel.add(chartTypeComboBox);
        JButton generateButton = new JButton("Generate Chart");
        controlsPanel.add(generateButton);
        panel.add(controlsPanel, BorderLayout.NORTH);

        // Display Area
        swapChartPanel = new JPanel(new BorderLayout());
        swapChartPanel.add(new JLabel("Select options and click 'Generate Chart' to see the effect of your swaps.", SwingConstants.CENTER));
        panel.add(swapChartPanel, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateSwapEffectChart());

        return panel;
    }

    private JPanel createCfgAlignmentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cfgStartDateSpinner = new JSpinner(new SpinnerDateModel());
        cfgEndDateSpinner = new JSpinner(new SpinnerDateModel());
        cfgStartDateSpinner.setEditor(new JSpinner.DateEditor(cfgStartDateSpinner, "yyyy-MM-dd"));
        cfgEndDateSpinner.setEditor(new JSpinner.DateEditor(cfgEndDateSpinner, "yyyy-MM-dd"));
        controlsPanel.add(new JLabel("Start Date:"));
        controlsPanel.add(cfgStartDateSpinner);
        controlsPanel.add(new JLabel("End Date:"));
        controlsPanel.add(cfgEndDateSpinner);
        JButton generateButton = new JButton("Compare My Plate");
        controlsPanel.add(generateButton);
        panel.add(controlsPanel, BorderLayout.NORTH);

        // Chart Display Area
        cfgChartPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // 1 row, 2 columns for side-by-side charts
        userPlatePanel = new JPanel(new BorderLayout());
        userPlatePanel.setBorder(new TitledBorder("Your Average Plate"));
        userPlatePanel.add(new JLabel("Generate a report to see your plate.", SwingConstants.CENTER));

        cfgPlatePanel = new JPanel(new BorderLayout());
        cfgPlatePanel.setBorder(new TitledBorder("CFG Recommended Plate"));
        cfgPlatePanel.add(new JLabel("Generate a report to see the recommendation.", SwingConstants.CENTER));

        cfgChartPanel.add(userPlatePanel);
        cfgChartPanel.add(cfgPlatePanel);
        panel.add(cfgChartPanel, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateCfgComparisonReport());

        return panel;
    }


    private void generateDailyIntakeReport() {
        Date startDate = (Date) intakeStartDateSpinner.getValue();
        Date endDate = (Date) intakeEndDateSpinner.getValue();
        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after end date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Generate Macro Chart
        DefaultPieDataset macroDataset = controller.createMacroNutrientDataset(startDate, endDate);
        macroChartPanel.removeAll();
        if (macroDataset.getKeys().stream().mapToDouble(key -> macroDataset.getValue((Comparable) key).doubleValue()).sum() == 0) {
            macroChartPanel.add(new JLabel("No data found for the selected period.", SwingConstants.CENTER));
        } else {
            chartRenderer.setFactory(new PieChartFactory("Macronutrient Distribution (g)"));
            JFreeChart macroChart = chartRenderer.renderChart(macroDataset);
            macroChartPanel.add(new ChartPanel(macroChart), BorderLayout.CENTER);
        }

        //Generate Micro Chart
        DefaultPieDataset microDataset = controller.createMicroNutrientDataset(startDate, endDate);
        microChartPanel.removeAll();
        if (microDataset.getItemCount() == 0) {
            microChartPanel.add(new JLabel("No other nutrient data found.", SwingConstants.CENTER));
        } else {
            chartRenderer.setFactory(new PieChartFactory("Top 5 Other Nutrients"));
            JFreeChart microChart = chartRenderer.renderChart(microDataset);
            microChartPanel.add(new ChartPanel(microChart), BorderLayout.CENTER);
        }

        // Update RDA Message
        String rdaMessage = controller.getRdaComparisonMessage(startDate, endDate);
        rdaLabel.setText(rdaMessage);

        revalidate();
        repaint();
    }

    private void generateSwapEffectChart() {
        Date startDate = (Date) swapStartDateSpinner.getValue();
        Date endDate = (Date) swapEndDateSpinner.getValue();
        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after end date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nutrient = (String) nutrientComboBox.getSelectedItem();
        String chartType = (String) chartTypeComboBox.getSelectedItem();
        DefaultCategoryDataset dataset = controller.createSwapEffectDataset(startDate, endDate, nutrient);

        if (dataset == null || dataset.getRowCount() == 0) {
            swapChartPanel.removeAll();
            swapChartPanel.add(new JLabel("No swapped meals found in the selected period to compare.", SwingConstants.CENTER));
            revalidate();
            repaint();
            return;
        }

        String title = "Effect of Swaps on " + nutrient + " Intake";
        if (chartType.equals("Bar Chart")) {
            chartRenderer.setFactory(new BarChartFactory(title, "Date", nutrient));
        } else {
            chartRenderer.setFactory(new LineChartFactory(title, "Date", nutrient));
        }

        JFreeChart chart = chartRenderer.renderChart(dataset);
        swapChartPanel.removeAll();
        swapChartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void generateCfgComparisonReport() {
        Date startDate = (Date) cfgStartDateSpinner.getValue();
        Date endDate = (Date) cfgEndDateSpinner.getValue();
        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after end date.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Generate User's Plate Chart
        DefaultPieDataset userDataset = controller.createCfgComparisonDataset(startDate, endDate);
        userPlatePanel.removeAll(); // Clear previous content

        if (userDataset.getItemCount() == 0) {
            userPlatePanel.add(new JLabel("No data for your plate in this period.", SwingConstants.CENTER));
        } else {
            chartRenderer.setFactory(new PieChartFactory("Your Average Plate Composition"));
            JFreeChart userChart = chartRenderer.renderChart(userDataset);
            userPlatePanel.add(new ChartPanel(userChart), BorderLayout.CENTER);
        }

        //Generate CFG Recommended Plate Chart
        DefaultPieDataset cfgDataset = cfgService.createRecommendedPlateDataset();
        cfgPlatePanel.removeAll(); // Clear previous content

        chartRenderer.setFactory(new PieChartFactory("CFG Recommended Plate"));
        JFreeChart cfgChart = chartRenderer.renderChart(cfgDataset);
        cfgPlatePanel.add(new ChartPanel(cfgChart), BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}