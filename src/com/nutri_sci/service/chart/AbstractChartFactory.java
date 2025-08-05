package com.nutri_sci.service.chart;

/**
 * Abstract base class for chart factories that provides common functionality.
 * Extracted from BarChartFactory and LineChartFactory to eliminate code duplication.
 */
public abstract class AbstractChartFactory implements IChartFactory {
    
    /** The title to display on the generated chart */
    protected final String title;
    
    /** The label for the category axis (typically the x-axis) */
    protected final String categoryAxisLabel;
    
    /** The label for the value axis (typically the y-axis) */
    protected final String valueAxisLabel;

    /**
     * Constructs a new AbstractChartFactory with the specified labels and title.
     * 
     * @param title the title to display on the chart
     * @param categoryAxisLabel the label for the category axis (x-axis)
     * @param valueAxisLabel the label for the value axis (y-axis)
     */
    protected AbstractChartFactory(String title, String categoryAxisLabel, String valueAxisLabel) {
        this.title = title;
        this.categoryAxisLabel = categoryAxisLabel;
        this.valueAxisLabel = valueAxisLabel;
    }

    // Getters for subclasses to access the fields
    protected String getTitle() { return title; }
    protected String getCategoryAxisLabel() { return categoryAxisLabel; }
    protected String getValueAxisLabel() { return valueAxisLabel; }
} 