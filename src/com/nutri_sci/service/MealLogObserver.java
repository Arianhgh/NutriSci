package com.nutri_sci.service;

/**
 * Observer interface for receiving notifications when meals are logged.
 * <p>
 * This interface defines the contract for components that need to be notified
 * when meal logging operations occur. Implementations of this interface can
 * register themselves with meal logging systems to receive automatic updates
 * when new meals are added to the system.
 * </p>
 * <p>
 * This interface is part of the Observer pattern implementation for meal data
 * management, allowing for loose coupling between meal logging operations and
 * dependent UI components or services that need to react to meal changes.
 * </p>
 * 
 * @author Juliett
 * @version 1.0
 * @since 1.0
 * @see com.nutri_sci.service.MealDataNotifier
 */
public interface MealLogObserver {
    
    /**
     * Called when a meal has been successfully logged to the system.
     * <p>
     * Implementations should use this callback to update their state,
     * refresh displayed data, or perform any other actions necessary
     * in response to new meal data being available.
     * </p>
     * <p>
     * This method should be designed to execute quickly and not block
     * the calling thread, as it may be called from UI event handlers
     * or other time-sensitive contexts.
     * </p>
     */
    void onMealLogged();
}