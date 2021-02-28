package org.openfinna.java.connector.classes.models.libraries;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Image implements Serializable {
    @SerializedName("url")
    private String url;
    @SerializedName("size")
    private long size;
    @SerializedName("resolution")
    private String resolution;

    public Image(String url, long size, String resolution) {
        this.url = url;
        this.size = size;
        this.resolution = resolution;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}
