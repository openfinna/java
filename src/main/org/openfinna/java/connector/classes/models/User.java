package org.openfinna.java.connector.classes.models;

import com.google.gson.annotations.SerializedName;
import org.openfinna.java.connector.classes.models.building.Building;
import org.openfinna.java.connector.classes.models.user.KirkesPreferences;
import org.openfinna.java.connector.classes.models.user.LibraryPreferences;

public class User {

    @SerializedName("name")
    private String name;

    @SerializedName("libraryPreferences")
    private LibraryPreferences libraryPreferences;

    @SerializedName("kirkesPreferences")
    private KirkesPreferences kirkesPreferences;

    private Building building;

    public User(String name, LibraryPreferences libraryPreferences, KirkesPreferences kirkesPreferences) {
        this.name = name;
        this.libraryPreferences = libraryPreferences;
        this.kirkesPreferences = kirkesPreferences;
    }

    public User() {

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
