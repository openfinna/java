package org.openfinna.java.connector.classes.models.user;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LibraryPreferences implements Serializable {

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("surname")
    private String surname;

    @SerializedName("address")
    private String address;

    @SerializedName("zipcode")
    private String zipcode;

    @SerializedName("city")
    private String city;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("email")
    private String email;

    public LibraryPreferences(String fullName, String firstName, String surname, String address, String zipcode, String city, String phoneNumber, String email) {
        this.fullName = fullName;
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.zipcode = zipcode;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public LibraryPreferences() {

    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
