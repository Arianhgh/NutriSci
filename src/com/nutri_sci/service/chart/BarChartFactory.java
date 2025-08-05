package com.nutri_sci.service.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;

/**
 * Factory implementation for creating bar charts for nutritional data visualization.
 * Refactored to extend AbstractChartFactory to eliminate code duplication.
 */
public class BarChartFactory extends AbstractChartFactory {

    /**
     * Constructs a new BarChartFactory with the specified labels and title.
     * 
     * @param title the title to display on the bar chart
     * @param categoryAxisLabel the label for the category axis (x-axis)
     * @param valueAxisLabel the label for the value axis (y-axis)
     */
    public BarChartFactory(String title, String categoryAxisLabel, String valueAxisLabel) {
        super(title, categoryAxisLabel, valueAxisLabel);
    }

    /**
     * Creates a vertical bar chart from the provided category dataset.
     */
    @Override
    public JFreeChart createChart(Dataset dataset) {
        return ChartFactory.createBarChart(
                getTitle(),
                getCategoryAxisLabel(),
                getValueAxisLabel(),
                (CategoryDataset) dataset,
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // generate tooltips
                false); // no URLs
    }
}