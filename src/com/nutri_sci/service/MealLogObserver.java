package com.nutri_sci.service;

/**
 * The Observer interface for the Observer design pattern.
 * UIs that need to be updated when a meal is logged will implement this.
 */
public interface MealLogObserver {
    void onMealLogged();
}