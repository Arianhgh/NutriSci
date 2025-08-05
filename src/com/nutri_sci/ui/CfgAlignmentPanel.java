package com.nutri_sci.ui;

import com.nutri_sci.controller.VisualizationController;
import com.nutri_sci.service.CanadaFoodGuideService;
import com.nutri_sci.service.ChartRenderer;
import com.nutri_sci.service.chart.PieChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for Canada Food Guide alignment visualization.
 * Extracted from VisualizationHubUI to follow Single Responsibility Principle.
 */
public class CfgAlignmentPanel extends JPanel {
    private final VisualizationController controller;
    private final ChartRenderer chartRenderer;
    private final CanadaFoodGuideService cfgService;

    // Components
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JPanel userPlatePanel;
    private JPanel cfgPlatePanel;

    public CfgAlignmentPanel(VisualizationController controller, ChartRenderer chartRenderer, CanadaFoodGuideService cfgService) {
        this.controller = controller;
        this.chartRenderer = chartRenderer;
        this.cfgService = cfgService;
        initializeComponents();
        layoutComponents();
    }

    private void initializeComponents() {
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));

        userPlatePanel = new JPanel(new BorderLayout());
        userPlatePanel.setBorder(new TitledBorder("Your Average Plate"));
        userPlatePanel.add(new JLabel("Generate a report to see your plate.", SwingConstants.CENTER));

        cfgPlatePanel = new JPanel(new BorderLayout());
        cfgPlatePanel.setBorder(new TitledBorder("CFG Recommended Plate"));
        cfgPlatePanel.add(new JLabel("Generate a report to see the recommendation.", SwingConstants.CENTER));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.add(new JLabel("Start Date:"));
        controlsPanel.add(startDateSpinner);
        controlsPanel.add(new JLabel("End Date:"));
        controlsPanel.add(endDateSpinner);
        JButton generateButton = new JButton("Compare My Plate");
        controlsPanel.add(generateButton);
        add(controlsPanel, BorderLayout.NORTH);

        // Chart Display Area
        JPanel chartPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        chartPanel.add(userPlatePanel);
        chartPanel.add(cfgPlatePanel);
        add(chartPanel, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateComparison());
    }

    public void setDefaultDate(Date date) {
        if (date != null) {
            startDateSpinner.setValue(date);
            endDateSpinner.setValue(date);
        }
    }

    private void generateComparison() {
        Date startDate = (Date) startDateSpinner.getValue();
        Date endDate = (Date) endDateSpinner.getValue();
        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after end date.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Color mapping and sequence for food groups
        Map<String, Color> colorMap = createColorMap();
        List<String> foodGroupSequence = createFoodGroupSequence();

        // Generate User's Plate Chart
        DefaultPieDataset userDatasetRaw = controller.createCfgComparisonDataset(startDate, endDate);
        userPlatePanel.removeAll();

        if (userDatasetRaw.getItemCount() == 0) {
            userPlatePanel.add(new JLabel("No data for your plate in this period.", SwingConstants.CENTER));
        } else {
            DefaultPieDataset userDataset = createOrderedDataset(userDatasetRaw, foodGroupSequence);
            chartRenderer.setFactory(new PieChartFactory("Your Average Plate Composition"));
            JFreeChart userChart = chartRenderer.renderChart(userDataset);
            applyColors(userChart, colorMap);
            userPlatePanel.add(new ChartPanel(userChart), BorderLayout.CENTER);
        }

        // Generate CFG Recommended Plate Chart
        DefaultPieDataset cfgDatasetRaw = cfgService.createRecommendedPlateDataset();
        cfgPlatePanel.removeAll();
        DefaultPieDataset cfgDataset = createOrderedDataset(cfgDatasetRaw, foodGroupSequence);

        chartRenderer.setFactory(new PieChartFactory("CFG Recommended Plate"));
        JFreeChart cfgChart = chartRenderer.renderChart(cfgDataset);
        applyColors(cfgChart, colorMap);
        cfgPlatePanel.add(new ChartPanel(cfgChart), BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private Map<String, Color> createColorMap() {
        Map<String, Color> colorMap = new HashMap<>();
        colorMap.put("Vegetables and Fruit", new Color(34, 139, 34)); // Forest Green
        colorMap.put("Grain Products", new Color(210, 105, 30));   // Chocolate
        colorMap.put("Milk and Alternatives", new Color(65, 105, 225));  // Royal Blue
        colorMap.put("Meat and Alternatives", new Color(178, 34, 34));   // Firebrick
        colorMap.put("Other", Color.GRAY);
        colorMap.put("Uncategorized", Color.LIGHT_GRAY);
        return colorMap;
    }

    private List<String> createFoodGroupSequence() {
        List<String> foodGroupSequence = new ArrayList<>();
        foodGroupSequence.add("Vegetables and Fruit");
        foodGroupSequence.add("Grain Products");
        foodGroupSequence.add("Milk and Alternatives");
        foodGroupSequence.add("Meat and Alternatives");
        foodGroupSequence.add("Other");
        foodGroupSequence.add("Uncategorized");
        return foodGroupSequence;
    }

    private DefaultPieDataset createOrderedDataset(DefaultPieDataset source, List<String> sequence) {
        DefaultPieDataset ordered = new DefaultPieDataset();
        for (String foodGroup : sequence) {
            if (source.getKeys().contains(foodGroup)) {
                ordered.setValue(foodGroup, source.getValue(foodGroup));
            }
        }
        return ordered;
    }

    private void applyColors(JFreeChart chart, Map<String, Color> colorMap) {
        PiePlot plot = (PiePlot) chart.getPlot();
        for (Object key : ((DefaultPieDataset) plot.getDataset()).getKeys()) {
            String foodGroup = (String) key;
            if (colorMap.containsKey(foodGroup)) {
                plot.setSectionPaint(foodGroup, colorMap.get(foodGroup));
            }
        }
    }
} 