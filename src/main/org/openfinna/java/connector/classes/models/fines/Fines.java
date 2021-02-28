package org.openfinna.java.connector.classes.models.fines;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Fines implements Serializable {
    private String currency;
    private double totalDue;
    private double payableDue;
    private List<Fine> fines = new ArrayList<>();

    public Fines(String currency, double totalDue, double payableDue, List<Fine> fines) {
        this.currency = currency;
        this.totalDue = totalDue;
        this.payableDue = payableDue;
        this.fines = fines;
    }

    public Fines() {
    }

    public List<Fine> getFines() {
        return fines;
    }

    public void setFines(List<Fine> fines) {
        this.fines = fines;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(double totalDue) {
        this.totalDue = totalDue;
    }

    public double getPayableDue() {
        return payableDue;
    }

    public void setPayableDue(double payableDue) {
        this.payableDue = payableDue;
    }
}
