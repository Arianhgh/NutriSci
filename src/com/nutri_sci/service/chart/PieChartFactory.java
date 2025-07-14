package com.nutri_sci.service.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.PieDataset;

public class PieChartFactory implements IChartFactory {
    private final String title;

    public PieChartFactory(String title) {
        this.title = title;
    }

    @Override
    public JFreeChart createChart(Dataset dataset) {
        return ChartFactory.createPieChart(
                title,
                (PieDataset) dataset,
                true, true, false);
    }
}