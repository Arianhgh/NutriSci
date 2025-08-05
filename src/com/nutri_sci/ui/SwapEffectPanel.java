package com.nutri_sci.ui;

import com.nutri_sci.controller.VisualizationController;
import com.nutri_sci.service.ChartRenderer;
import com.nutri_sci.service.chart.BarChartFactory;
import com.nutri_sci.service.chart.LineChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;

/**
 * Panel for swap effect analysis visualization.
 * Extracted from VisualizationHubUI to follow Single Responsibility Principle.
 */
public class SwapEffectPanel extends JPanel {
    private final VisualizationController controller;
    private final ChartRenderer chartRenderer;

    // Components
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JComboBox<String> nutrientComboBox;
    private JComboBox<String> chartTypeComboBox;
    private JPanel chartPanel;

    public SwapEffectPanel(VisualizationController controller, ChartRenderer chartRenderer) {
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

        nutrientComboBox = new JComboBox<>(new String[]{"Calories", "Protein", "Fiber"});
        chartTypeComboBox = new JComboBox<>(new String[]{"Bar Chart", "Line Chart"});

        chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(new JLabel("Select options and click 'Generate Chart' to see the effect of your swaps.", SwingConstants.CENTER));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.add(new JLabel("Start:"));
        controlsPanel.add(startDateSpinner);
        controlsPanel.add(new JLabel("End:"));
        controlsPanel.add(endDateSpinner);
        controlsPanel.add(new JLabel("Nutrient:"));
        controlsPanel.add(nutrientComboBox);
        controlsPanel.add(new JLabel("Chart Type:"));
        controlsPanel.add(chartTypeComboBox);
        JButton generateButton = new JButton("Generate Chart");
        controlsPanel.add(generateButton);
        add(controlsPanel, BorderLayout.NORTH);

        // Display Area
        add(chartPanel, BorderLayout.CENTER);

        generateButton.addActionListener(e -> generateChart());
    }

    public void setDefaultDate(Date date) {
        if (date != null) {
            startDateSpinner.setValue(date);
            endDateSpinner.setValue(date);
        }
    }

    private void generateChart() {
        Date startDate = (Date) startDateSpinner.getValue();
        Date endDate = (Date) endDateSpinner.getValue();
        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after end date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nutrient = (String) nutrientComboBox.getSelectedItem();
        String chartType = (String) chartTypeComboBox.getSelectedItem();
        DefaultCategoryDataset dataset = controller.createSwapEffectDataset(startDate, endDate, nutrient);

        if (dataset == null || dataset.getRowCount() == 0) {
            chartPanel.removeAll();
            chartPanel.add(new JLabel("No swapped meals found in the selected period to compare.", SwingConstants.CENTER));
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
        chartPanel.removeAll();
        chartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);

        revalidate();
        repaint();
    }
} 