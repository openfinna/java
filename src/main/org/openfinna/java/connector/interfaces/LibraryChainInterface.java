package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.building.Building;

import java.util.List;

public interface LibraryChainInterface {
    void onFetchDefaultLibraryBuilding(Building building);

    void onFetchLibraryBuildings(List<Building> buildingList);

    void onError(Exception e);
}
