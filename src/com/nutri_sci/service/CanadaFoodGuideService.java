package com.nutri_sci.service;

import org.jfree.data.general.DefaultPieDataset;

/**
 * Service providing data and recommendations from Canada's Food Guide.
 * <p>
 * This service encapsulates the official dietary recommendations from Canada's Food Guide,
 * making them easily accessible throughout the application for comparison with user
 * dietary intake and visualization purposes. The service provides standardized datasets
 * that can be used directly with chart factories for visualization.
 * </p>
 * <p>
 * The recommendations are based on the current Canada's Food Guide guidelines which
 * emphasize the proportional representation of different food groups on a healthy plate.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see <a href="https://food-guide.canada.ca/">Canada's Food Guide</a>
 */
public class CanadaFoodGuideService {

    /**
     * Creates a pie dataset representing the recommended proportions of food groups
     * according to Canada's Food Guide.
     * <p>
     * This dataset reflects the "healthy plate" model promoted by Canada's Food Guide,
     * where different food groups should occupy specific proportions of a meal:
     * <ul>
     *   <li>Vegetables and Fruit: 50% of the plate</li>
     *   <li>Grain Products: 25% of the plate</li>
     *   <li>Milk and Alternatives: 12.5% of the plate</li>
     *   <li>Meat and Alternatives: 12.5% of the plate</li>
     * </ul>
     * </p>
     * <p>
     * The returned dataset can be used directly with pie chart factories to create
     * visual representations of recommended dietary proportions for comparison with
     * user meal data.
     * </p>
     * 
     * @return a DefaultPieDataset containing the recommended food group proportions
     *         as percentages of a healthy plate according to Canada's Food Guide
     */
    public DefaultPieDataset createRecommendedPlateDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Vegetables and Fruit", 50.0);
        dataset.setValue("Grain Products", 25.0);
        dataset.setValue("Milk and Alternatives", 12.5);
        dataset.setValue("Meat and Alternatives", 12.5);
        return dataset;
    }
}