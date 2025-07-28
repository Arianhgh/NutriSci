package com.nutri_sci.service.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.PieDataset;

/**
 * Factory implementation for creating pie charts for nutritional data visualization.
 * <p>
 * This factory creates pie charts typically used to show the distribution of
 * different nutrients or food groups within a meal or across multiple meals.
 * Pie charts are particularly effective for showing proportional relationships
 * and relative contributions of different components to the total.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see IChartFactory
 */
public class PieChartFactory implements IChartFactory {
    
    /** The title to display on the generated pie chart */
    private final String title;

    /**
     * Constructs a new PieChartFactory with the specified title.
     * 
     * @param title the title to display on the pie chart
     */
    public PieChartFactory(String title) {
        this.title = title;
    }

    /**
     * Creates a pie chart from the provided pie dataset.
     * <p>
     * The chart is created with standard settings suitable for nutritional
     * data visualization, including legends and tooltips enabled for better
     * user interaction.
     * </p>
     * 
     * @param dataset the pie dataset containing the data to visualize
     * @return a configured pie chart ready for display
     * @throws ClassCastException if the dataset is not a PieDataset
     */
    @Override
    public JFreeChart createChart(Dataset dataset) {
        return ChartFactory.createPieChart(
                title,
                (PieDataset) dataset,
                true, // include legend
                true, // generate tooltips
                false); // no URLs
    }
}