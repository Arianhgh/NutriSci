package com.nutri_sci.service.chart;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

public interface IChartFactory {
    JFreeChart createChart(Dataset dataset);
}