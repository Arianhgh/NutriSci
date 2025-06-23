package com.nutri_sci.service;

import java.util.Observable;

/**
 * Implements the Subject (or Observable) part of the Observer pattern.
 * This class notifies observers when the meal data has been updated.
 * It is a Singleton to ensure all parts of the app notify through the same channel.
 */
public class MealDataNotifier extends Observable {
    private static MealDataNotifier instance;

    private MealDataNotifier() {}

    public static synchronized MealDataNotifier getInstance() {
        if (instance == null) {
            instance = new MealDataNotifier();
        }
        return instance;
    }

    /**
     * Notifies all registered observers that a change has occurred.
     */
    public void notifyObserversOfChange() {
        setChanged(); // Mark this object as having been changed
        notifyObservers();
    }
}