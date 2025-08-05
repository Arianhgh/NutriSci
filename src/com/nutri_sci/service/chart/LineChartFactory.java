package com.nutri_sci.service.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;

/**
 * Factory implementation for creating line charts for nutritional data visualization.
 * Refactored to extend AbstractChartFactory to eliminate code duplication.
 */
public class LineChartFactory extends AbstractChartFactory {

    /**
     * Constructs a new LineChartFactory with the specified labels and title.
     * 
     * @param title the title to display on the line chart
     * @param categoryAxisLabel the label for the category axis (x-axis)
     * @param valueAxisLabel the label for the value axis (y-axis)
     */
    public LineChartFactory(String title, String categoryAxisLabel, String valueAxisLabel) {
        super(title, categoryAxisLabel, valueAxisLabel);
    }

    /**
     * Creates a line chart from the provided category dataset.
     */
    @Override
    public JFreeChart createChart(Dataset dataset) {
        return ChartFactory.createLineChart(
                getTitle(),
                getCategoryAxisLabel(),
                getValueAxisLabel(),
                (CategoryDataset) dataset);
    }
}