package org.openfinna.java.connector.classes.models.building;

import com.google.gson.annotations.SerializedName;

public class Building {
    @SerializedName("value")
    private String id;
    @SerializedName("displayText")
    private String name;

    public Building(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
