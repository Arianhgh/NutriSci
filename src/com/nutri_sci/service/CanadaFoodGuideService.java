package com.nutri_sci.service;

import org.jfree.data.general.DefaultPieDataset;

/**
 * Provides data related to Canada's Food Guide recommendations.
 * This service encapsulates the fixed values from the food guide for easy retrieval.
 */
public class CanadaFoodGuideService {

    public DefaultPieDataset createRecommendedPlateDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Vegetables and Fruit", 50.0);
        dataset.setValue("Grain Products", 25.0);
        dataset.setValue("Milk and Alternatives", 12.5);
        dataset.setValue("Meat and Alternatives", 12.5);
        return dataset;
    }
}