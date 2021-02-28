package org.openfinna.java.connector.classes.models.building;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Building implements Serializable {
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

    public String getRawId() {
        String[] split = id.split("/");
        return split[1];
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
