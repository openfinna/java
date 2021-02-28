package org.openfinna.java.connector.classes.models.holds;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PickupLocation implements Serializable {

    @SerializedName("locationID")
    private String id;

    @SerializedName("locationDisplay")
    private String name;

    public PickupLocation(String id, String name) {
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
