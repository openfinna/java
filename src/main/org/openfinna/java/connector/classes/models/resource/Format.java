package org.openfinna.java.connector.classes.models.resource;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Format implements Serializable {
    @SerializedName("value")
    private String id;
    @SerializedName("translated")
    private String translated;

    public Format(String id, String translated) {
        this.id = id;
        this.translated = translated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTranslated() {
        return translated;
    }

    public void setTranslated(String translated) {
        this.translated = translated;
    }
}
