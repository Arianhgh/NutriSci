package com.nutri_sci.service.chart;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

/**
 * Factory interface for creating JFreeChart instances from datasets.
 * <p>
 * This interface defines the contract for chart factories that create different
 * types of charts (pie, bar, line) for visualizing nutritional data. Each
 * implementation is responsible for creating a specific chart type with
 * appropriate styling and configuration for the NutriSci application.
 * </p>
 * <p>
 * The factory pattern allows for easy extension of chart types and provides
 * a consistent interface for chart creation across the visualization system.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see org.jfree.chart.JFreeChart
 * @see org.jfree.data.general.Dataset
 */
public interface IChartFactory {
    
    /**
     * Creates a JFreeChart from the provided dataset.
     * <p>
     * Implementations should create an appropriately styled chart for the
     * NutriSci application, including proper titles, legends, colors, and
     * formatting suitable for nutritional data visualization.
     * </p>
     * 
     * @param dataset the data to be visualized in the chart
     * @return a configured JFreeChart ready for display
     */
    JFreeChart createChart(Dataset dataset);
}