package org.openfinna.java.connector.classes.models;

import com.google.gson.annotations.SerializedName;
import org.openfinna.java.connector.classes.models.building.Building;
import org.openfinna.java.connector.classes.models.holds.PickupLocation;
import org.openfinna.java.connector.classes.models.user.KirkesPreferences;
import org.openfinna.java.connector.classes.models.user.LibraryPreferences;

import java.util.List;

public class User {

    @SerializedName("name")
    private String name;

    private List<PickupLocation> pickupLocations;

    @SerializedName("libraryPreferences")
    private LibraryPreferences libraryPreferences;

    @SerializedName("kirkesPreferences")
    private KirkesPreferences kirkesPreferences;

    private Building building;

    public User(String name, List<PickupLocation> pickupLocations, LibraryPreferences libraryPreferences, KirkesPreferences kirkesPreferences, Building building) {
        this.name = name;
        this.pickupLocations = pickupLocations;
        this.libraryPreferences = libraryPreferences;
        this.kirkesPreferences = kirkesPreferences;
        this.building = building;
    }

    public User() {

    }

    public List<PickupLocation> getPickupLocations() {
        return pickupLocations;
    }

    public void setPickupLocations(List<PickupLocation> pickupLocations) {
        this.pickupLocations = pickupLocations;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LibraryPreferences getLibraryPreferences() {
        return libraryPreferences;
    }

    public void setLibraryPreferences(LibraryPreferences libraryPreferences) {
        this.libraryPreferences = libraryPreferences;
    }

    public KirkesPreferences getKirkesPreferences() {
        return kirkesPreferences;
    }

    public void setKirkesPreferences(KirkesPreferences kirkesPreferences) {
        this.kirkesPreferences = kirkesPreferences;
    }
}
