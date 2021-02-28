package org.openfinna.java.connector.classes.models.libraries;

import java.io.Serializable;

public class LibraryLocation implements Serializable {
    private String street;
    private String zipcode;
    private String city;
    private String mapsUrl;
    private String matkaFiUrl;
    private Coordinates coordinates;


    public LibraryLocation(String street, String zipcode, String city, String mapsUrl, String matkaFiUrl, Coordinates coordinates) {
        this.street = street;
        this.zipcode = zipcode;
        this.city = city;
        this.mapsUrl = mapsUrl;
        this.matkaFiUrl = matkaFiUrl;
        this.coordinates = coordinates;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMapsUrl() {
        return mapsUrl;
    }

    public void setMapsUrl(String mapsUrl) {
        this.mapsUrl = mapsUrl;
    }

    public String getMatkaFiUrl() {
        return matkaFiUrl;
    }

    public void setMatkaFiUrl(String matkaFiUrl) {
        this.matkaFiUrl = matkaFiUrl;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public static class Coordinates implements Serializable {
        private double lat;
        private double lon;

        public Coordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }
    }
}
