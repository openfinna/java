package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.holds.HoldingDetails;
import org.openfinna.java.connector.classes.models.holds.PickupLocation;

import java.util.List;

public interface PickupLocationsInterface {
    void onFetchPickupLocations(List<PickupLocation> locations, HoldingDetails holdingDetails, PickupLocation defaultLocation);

    void onFetchDefaultPickupLocation(PickupLocation defaultLocation, List<PickupLocation> allLocations);

    void onError(Exception e);
}
