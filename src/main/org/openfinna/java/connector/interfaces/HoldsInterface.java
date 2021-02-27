package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.holds.Hold;

import java.util.List;

public interface HoldsInterface {
    void onGetHolds(List<Hold> holds);

    /**
     * Callback for pickup location changing
     * @param hold Hold of which location changed (notice! new location is not included, it is the same object you set as a parameter)
     */
    void onChangePickupLocation(Hold hold);

    void onError(Exception e);
}
