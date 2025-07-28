package com.nutri_sci.service;

import com.nutri_sci.service.chart.IChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

/**
 * Service for rendering charts using configurable chart factories.
 * <p>
 * The ChartRenderer acts as a context in the Strategy pattern, allowing for
 * dynamic selection of chart types at runtime. By accepting different chart
 * factory implementations, this service can create various types of charts
 * (pie, bar, line) from the same dataset without code duplication.
 * </p>
 * <p>
 * This design provides flexibility in chart creation and makes it easy to
 * add new chart types or modify existing ones without affecting client code.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see IChartFactory
 * @see com.nutri_sci.service.chart.PieChartFactory
 * @see com.nutri_sci.service.chart.BarChartFactory
 * @see com.nutri_sci.service.chart.LineChartFactory
 */
public class ChartRenderer {
    
    /** The currently configured chart factory for creating charts */
    private IChartFactory factory;

    /**
     * Sets the chart factory to be used for chart creation.
     * <p>
     * This method allows runtime configuration of the chart type by setting
     * different factory implementations. The factory will be used for all
     * subsequent calls to {@link #renderChart(Dataset)} until changed.
     * </p>
     * 
     * @param factory the chart factory to use for creating charts
     */
    public void setFactory(IChartFactory factory) {
        this.factory = factory;
    }

    /**
     * Renders a chart from the provided dataset using the configured factory.
     * <p>
     * This method delegates chart creation to the currently set factory.
     * The type of chart created depends on the factory implementation that
     * was set via {@link #setFactory(IChartFactory)}.
     * </p>
     * 
     * @param dataset the dataset containing the data to visualize
     * @return a JFreeChart instance ready for display, or null if no factory
     *         is set or the dataset is null
     */
    public JFreeChart renderChart(Dataset dataset) {
        if (factory == null || dataset == null) {
            return null;
        }
        return factory.createChart(dataset);
    }
}