package com.nutri_sci.service.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;

public class BarChartFactory implements IChartFactory {
    private final String title;
    private final String categoryAxisLabel;
    private final String valueAxisLabel;

    public BarChartFactory(String title, String categoryAxisLabel, String valueAxisLabel) {
        this.title = title;
        this.categoryAxisLabel = categoryAxisLabel;
        this.valueAxisLabel = valueAxisLabel;
    }

    @Override
    public JFreeChart createChart(Dataset dataset) {
        return ChartFactory.createBarChart(
                title,
                categoryAxisLabel,
                valueAxisLabel,
                (CategoryDataset) dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
    }
}