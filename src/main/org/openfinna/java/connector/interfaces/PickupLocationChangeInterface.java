package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.holds.PickupLocation;

public interface PickupLocationChangeInterface {
    void onPickupLocationChange(PickupLocation pickupLocation);

    void onError(Exception e);
}
