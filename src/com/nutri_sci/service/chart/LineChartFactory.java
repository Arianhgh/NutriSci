package com.nutri_sci.service.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;

/**
 * Factory implementation for creating line charts for nutritional data visualization.
 * <p>
 * This factory creates line charts typically used to show trends and changes in
 * nutritional intake over time. Line charts are particularly effective for tracking
 * progress toward nutritional goals, monitoring changes in dietary patterns, and
 * visualizing temporal relationships in nutrition data.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see IChartFactory
 */
public class LineChartFactory implements IChartFactory {
    
    /** The title to display on the generated line chart */
    private final String title;
    
    /** The label for the category axis (typically the x-axis, often time-based) */
    private final String categoryAxisLabel;
    
    /** The label for the value axis (typically the y-axis, showing nutritional values) */
    private final String valueAxisLabel;

    /**
     * Constructs a new LineChartFactory with the specified labels and title.
     * 
     * @param title the title to display on the line chart
     * @param categoryAxisLabel the label for the category axis (x-axis)
     * @param valueAxisLabel the label for the value axis (y-axis)
     */
    public LineChartFactory(String title, String categoryAxisLabel, String valueAxisLabel) {
        this.title = title;
        this.categoryAxisLabel = categoryAxisLabel;
        this.valueAxisLabel = valueAxisLabel;
    }

    /**
     * Creates a line chart from the provided category dataset.
     * <p>
     * The chart is configured with default line chart settings suitable for
     * showing trends over time or across categories. The chart automatically
     * includes appropriate scaling and formatting for nutritional data display.
     * </p>
     * 
     * @param dataset the category dataset containing the data to visualize
     * @return a configured line chart ready for display
     * @throws ClassCastException if the dataset is not a CategoryDataset
     */
    @Override
    public JFreeChart createChart(Dataset dataset) {
        return ChartFactory.createLineChart(
                title,
                categoryAxisLabel,
                valueAxisLabel,
                (CategoryDataset) dataset);
    }
}