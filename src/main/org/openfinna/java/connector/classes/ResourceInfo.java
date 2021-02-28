package org.openfinna.java.connector.classes;

import org.json.JSONObject;
import org.openfinna.java.connector.classes.models.resource.Author;
import org.openfinna.java.connector.classes.models.resource.Format;

import java.io.Serializable;
import java.util.List;

public class ResourceInfo implements Serializable {

    private String id;
    private String title;
    private String shortTitle;
    private String subTitle;
    private List<String> topics;
    private int publicationYear;
    private String isbn;
    private List<Author> authors;
    private List<Format> formats;
    private List<String> generalNotes;
    private List<String> languages;
    private List<String> originalLanguages;
    private String physicalDescription;
    private String edition;
    private String manufacturer;
    private List<String> publishers;
    private String publicationPlace;
    private List<String> ykl;
    private List<String> awards;
    private String imageLink;

    private JSONObject rawData;

    public ResourceInfo(String id, String title, String shortTitle, String subTitle, List<String> topics, int publicationYear, String isbn, List<Author> authors, List<Format> formats, List<String> generalNotes, List<String> languages, List<String> originalLanguages, String physicalDescription, String edition, String manufacturer, List<String> publishers, String publicationPlace, List<String> ykl, List<String> awards, String imageLink, JSONObject rawData) {
        this.id = id;
        this.title = title;
        this.shortTitle = shortTitle;
        this.subTitle = subTitle;
        this.topics = topics;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.authors = authors;
        this.formats = formats;
        this.generalNotes = generalNotes;
        this.languages = languages;
        this.originalLanguages = originalLanguages;
        this.physicalDescription = physicalDescription;
        this.edition = edition;
        this.manufacturer = manufacturer;
        this.publishers = publishers;
        this.publicationPlace = publicationPlace;
        this.ykl = ykl;
        this.awards = awards;
        this.imageLink = imageLink;
        this.rawData = rawData;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
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

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public List<Format> getFormats() {
        return formats;
    }

    public void setFormats(List<Format> formats) {
        this.formats = formats;
    }

    public List<String> getGeneralNotes() {
        return generalNotes;
    }

    public void setGeneralNotes(List<String> generalNotes) {
        this.generalNotes = generalNotes;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getOriginalLanguages() {
        return originalLanguages;
    }

    public void setOriginalLanguages(List<String> originalLanguages) {
        this.originalLanguages = originalLanguages;
    }

    public String getPhysicalDescription() {
        return physicalDescription;
    }

    public void setPhysicalDescription(String physicalDescription) {
        this.physicalDescription = physicalDescription;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public List<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }

    public String getPublicationPlace() {
        return publicationPlace;
    }

    public void setPublicationPlace(String publicationPlace) {
        this.publicationPlace = publicationPlace;
    }

    public List<String> getYkl() {
        return ykl;
    }

    public void setYkl(List<String> ykl) {
        this.ykl = ykl;
    }

    public List<String> getAwards() {
        return awards;
    }

    public void setAwards(List<String> awards) {
        this.awards = awards;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public JSONObject getRawData() {
        return rawData;
    }

    public void setRawData(JSONObject rawData) {
        this.rawData = rawData;
    }
}
