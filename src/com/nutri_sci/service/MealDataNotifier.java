package com.nutri_sci.service;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Implements the Subject part of the Observer pattern for meal data updates.
 * <p>
 * This class serves as a central notification hub for meal data changes throughout
 * the application. It uses Java's PropertyChangeSupport mechanism to notify registered
 * listeners when meal data has been modified, ensuring that all UI components and
 * dependent services can react appropriately to data changes.
 * </p>
 * <p>
 * The class follows the Singleton pattern to ensure that all parts of the application
 * use the same notification channel, preventing data synchronization issues and
 * ensuring consistent behavior across the entire system.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see java.beans.PropertyChangeSupport
 * @see java.beans.PropertyChangeListener
 */
public class MealDataNotifier {
    
    /** Single instance of the notifier (Singleton pattern) */
    private static MealDataNotifier instance;
    
    /** Property change support for managing observers */
    private final PropertyChangeSupport support;

    /**
     * Private constructor to prevent direct instantiation (Singleton pattern).
     * Initializes the PropertyChangeSupport with this instance as the source.
     */
    private MealDataNotifier() {
        support = new PropertyChangeSupport(this);
    }

    /**
     * Gets the singleton instance of the MealDataNotifier.
     * <p>
     * This method provides thread-safe lazy initialization of the singleton instance.
     * The instance is created only when first requested and subsequent calls return
     * the same instance.
     * </p>
     * 
     * @return the singleton MealDataNotifier instance
     */
    public static synchronized MealDataNotifier getInstance() {
        if (instance == null) {
            instance = new MealDataNotifier();
        }
        return instance;
    }

    /**
     * Registers a PropertyChangeListener to receive meal data change notifications.
     * <p>
     * Listeners registered through this method will be notified whenever
     * {@link #notifyMealDataChanged()} is called. This allows UI components
     * and other services to stay synchronized with meal data updates.
     * </p>
     * 
     * @param pcl the PropertyChangeListener to register
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    /**
     * Unregisters a PropertyChangeListener from receiving notifications.
     * <p>
     * This method removes a previously registered listener. Once removed,
     * the listener will no longer receive meal data change notifications.
     * </p>
     * 
     * @param pcl the PropertyChangeListener to unregister
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    /**
     * Notifies all registered listeners that meal data has changed.
     * <p>
     * This method should be called whenever meal data is added, modified, or deleted.
     * It triggers a property change event with the property name "mealData", allowing
     * listeners to respond appropriately to the data change.
     * </p>
     * <p>
     * The old and new values are set to null since this is a generic change notification
     * rather than a specific value change. Listeners are expected to refresh their
     * data from the appropriate sources when notified.
     * </p>
     */
    public void notifyMealDataChanged() {
        // The property name "mealData" can be used by listeners to identify the change.
        // Old and new values are null as we are just signaling a generic change.
        support.firePropertyChange("mealData", null, null);
    }
}