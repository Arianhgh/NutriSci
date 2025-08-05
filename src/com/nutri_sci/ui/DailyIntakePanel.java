package com.nutri_sci.ui;

import com.nutri_sci.controller.VisualizationController;
import com.nutri_sci.service.ChartRenderer;
import com.nutri_sci.service.chart.PieChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Date;

/**
 * Panel for daily intake analysis visualization.
 * Extracted from VisualizationHubUI to follow Single Responsibility Principle.
 */
public class DailyIntakePanel extends JPanel {
    private final VisualizationController controller;
    private final ChartRenderer chartRenderer;

    // Components
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JPanel macroChartPanel;
    private JPanel microChartPanel;
    private JLabel rdaLabel;

    public DailyIntakePanel(VisualizationController controller, ChartRenderer chartRenderer) {
        this.controller = controller;
        this.chartRenderer = chartRenderer;
        initializeComponents();
        layoutComponents();
    }

    private void initializeComponents() {
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));

        macroChartPanel = new JPanel(new BorderLayout());
        microChartPanel = new JPanel(new BorderLayout());
        macroChartPanel.setBorder(new TitledBorder("Macronutrient Distribution"));
        microChartPanel.setBorder(new TitledBorder("Top 5 Other Nutrients"));

        macroChartPanel.add(new JLabel("Generate a report to see your macro breakdown.", SwingConstants.CENTER));
        microChartPanel.add(new JLabel("Generate a report to see your micro breakdown.", SwingConstants.CENTER));

        rdaLabel = new JLabel(" ", SwingConstants.CENTER);
        rdaLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
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
        JButton generateButton = new JButton("Generate Report");
        controlsPanel.add(generateButton);
        add(controlsPanel, BorderLayout.NORTH);

        // Display Area
        JPanel displayPanel = new JPanel(new BorderLayout(10, 10));

        // Container for the two pie charts
        JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 10, 10));
        chartsContainer.add(macroChartPanel);
        chartsContainer.add(microChartPanel);

        displayPanel.add(chartsContainer, BorderLayout.CENTER);
        displayPanel.add(rdaLabel, BorderLayout.SOUTH);
        add(displayPanel, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateReport());
    }

    public void setDefaultDate(Date date) {
        if (date != null) {
            startDateSpinner.setValue(date);
            endDateSpinner.setValue(date);
        }
    }

    private void generateReport() {
        Date startDate = (Date) startDateSpinner.getValue();
        Date endDate = (Date) endDateSpinner.getValue();
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

        // Generate Micro Chart
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
} 