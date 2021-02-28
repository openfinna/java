package org.openfinna.java.connector.classes.models.libraries;

import com.google.gson.annotations.SerializedName;

public class Link {
    @SerializedName("url")
    private String url;
    @SerializedName("name")
    private String name;

    public Link(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
