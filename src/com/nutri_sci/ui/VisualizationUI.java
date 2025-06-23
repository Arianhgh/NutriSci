package com.nutri_sci.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.Color;

/**
 * A UI to demonstrate a working JFreeChart visualization.
 */
public class VisualizationUI extends JFrame {

    public VisualizationUI() {
        setTitle("Nutrient Visualization");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JFreeChart barChart = createChart(createDataset());

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.white);
        setContentPane(chartPanel);
    }

    /**
     * Creates a sample dataset for the bar chart.
     * This version includes multiple nutrient types for comparison.
     */
    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series1 = "Calories (kcal)";
        String series2 = "Protein (g)";
        String series3 = "Carbohydrates (g)";

        // Pre-populated data for demonstration
        dataset.addValue(550, series1, "Breakfast");
        dataset.addValue(30, series2, "Breakfast");
        dataset.addValue(60, series3, "Breakfast");

        dataset.addValue(750, series1, "Lunch");
        dataset.addValue(45, series2, "Lunch");
        dataset.addValue(80, series3, "Lunch");

        dataset.addValue(650, series1, "Dinner");
        dataset.addValue(50, series2, "Dinner");
        dataset.addValue(55, series3, "Dinner");

        dataset.addValue(250, series1, "Snack");
        dataset.addValue(10, series2, "Snack");
        dataset.addValue(30, series3, "Snack");

        return dataset;
    }

    /**
     * Creates a JFreeChart object.
     */
    private JFreeChart createChart(DefaultCategoryDataset dataset) {
        return ChartFactory.createBarChart(
                "Nutrient Breakdown Per Meal", // Chart title
                "Meal Type",                   // X-axis label
                "Amount",                      // Y-axis label
                dataset,
                PlotOrientation.VERTICAL,
                true,  // Include legend
                true,  // Generate tooltips
                false  // No URLs
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VisualizationUI().setVisible(true));
    }
}