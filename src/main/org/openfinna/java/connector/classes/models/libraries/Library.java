package org.openfinna.java.connector.classes.models.libraries;

import org.openfinna.java.connector.classes.models.libraries.schedule.Day;

import java.util.List;

public class Library {
    private String id;
    private String name;
    private String shortName;
    private String slug;
    private LibraryType type;
    private String email;
    private String homepage;
    private LibraryLocation libraryLocation;
    private List<Image> images;
    private List<Link> links;
    private List<String> services;
    private List<String> scheduleNotices;
    private String slogan;
    private List<Day> days;

    public Library(String id, String name, String shortName, String slug, LibraryType type, String email, String homepage, LibraryLocation libraryLocation, List<Image> images, List<Link> links, List<String> services, List<String> scheduleNotices, String slogan, List<Day> days) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.slug = slug;
        this.type = type;
        this.email = email;
        this.homepage = homepage;
        this.libraryLocation = libraryLocation;
        this.images = images;
        this.links = links;
        this.services = services;
        this.scheduleNotices = scheduleNotices;
        this.slogan = slogan;
        this.days = days;
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public LibraryType getType() {
        return type;
    }

    public void setType(LibraryType type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public LibraryLocation getLibraryLocation() {
        return libraryLocation;
    }

    public void setLibraryLocation(LibraryLocation libraryLocation) {
        this.libraryLocation = libraryLocation;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public List<String> getScheduleNotices() {
        return scheduleNotices;
    }

    public void setScheduleNotices(List<String> scheduleNotices) {
        this.scheduleNotices = scheduleNotices;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }
}
