package org.openfinna.java.connector.classes.models;

import com.google.gson.annotations.SerializedName;

public class Resource {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("author")
    private String author;

    @SerializedName("type")
    private String type;

    @SerializedName("image")
    private String image;

    public Resource(String id, String title, String author, String type, String image) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.type = type;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
