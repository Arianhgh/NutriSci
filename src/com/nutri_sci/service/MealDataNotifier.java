package com.nutri_sci.service;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Implements the Subject part of the Observer pattern using PropertyChangeSupport.
 * This class notifies listeners when the meal data has been updated.
 * It is a Singleton to ensure all parts of the app notify through the same channel.
 */
public class MealDataNotifier {
    private static MealDataNotifier instance;
    private final PropertyChangeSupport support;

    private MealDataNotifier() {
        support = new PropertyChangeSupport(this);
    }

    public static synchronized MealDataNotifier getInstance() {
        if (instance == null) {
            instance = new MealDataNotifier();
        }
        return instance;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    /**
     * Notifies all registered listeners that a change has occurred.
     */
    public void notifyMealDataChanged() {
        // The property name "mealData" can be used by listeners to identify the change.
        // Old and new values are null as we are just signaling a generic change.
        support.firePropertyChange("mealData", null, null);
    }
}