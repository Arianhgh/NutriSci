package com.nutri_sci.service;

import com.nutri_sci.service.chart.IChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

public class ChartRenderer {
    private IChartFactory factory;

    public void setFactory(IChartFactory factory) {
        this.factory = factory;
    }

    public JFreeChart renderChart(Dataset dataset) {
        if (factory == null || dataset == null) {
            return null;
        }
        return factory.createChart(dataset);
    }
}