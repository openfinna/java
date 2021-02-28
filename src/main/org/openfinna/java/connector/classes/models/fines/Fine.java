package org.openfinna.java.connector.classes.models.fines;

import java.util.Date;

public class Fine {
    private double price = -1;
    private Date registrationDate;
    private String description;

    public Fine(double price, Date registrationDate, String description) {
        this.price = price;
        this.registrationDate = registrationDate;
        this.description = description;
    }

    public Fine() {
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
