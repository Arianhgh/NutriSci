package com.nutri_sci.service.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;

public class LineChartFactory implements IChartFactory {
    private final String title;
    private final String categoryAxisLabel;
    private final String valueAxisLabel;

    public LineChartFactory(String title, String categoryAxisLabel, String valueAxisLabel) {
        this.title = title;
        this.categoryAxisLabel = categoryAxisLabel;
        this.valueAxisLabel = valueAxisLabel;
    }

    @Override
    public JFreeChart createChart(Dataset dataset) {
        return ChartFactory.createLineChart(
                title,
                categoryAxisLabel,
                valueAxisLabel,
                (CategoryDataset) dataset);
    }
}