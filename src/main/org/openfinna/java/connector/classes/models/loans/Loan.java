package org.openfinna.java.connector.classes.models.loans;

import com.google.gson.annotations.SerializedName;
import org.openfinna.java.connector.classes.models.Resource;

import java.util.Date;

public class Loan {

    @SerializedName("id")
    private String id;

    @SerializedName("renewId")
    private String renewId;

    @SerializedName("resource")
    private Resource resource;

    @SerializedName("renewsTotal")
    private int renewsTotal;

    @SerializedName("renewsUsed")
    private int renewsUsed;

    private Date dueDate;

    public Loan(String id, String renewId, Resource resource, int renewsTotal, int renewsUsed, Date dueDate) {
        this.id = id;
        this.renewId = renewId;
        this.resource = resource;
        this.renewsTotal = renewsTotal;
        this.renewsUsed = renewsUsed;
        this.dueDate = dueDate;
    }

    public Loan() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRenewId() {
        return renewId;
    }

    public void setRenewId(String renewId) {
        this.renewId = renewId;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public int getRenewsTotal() {
        return renewsTotal;
    }

    public void setRenewsTotal(int renewsTotal) {
        this.renewsTotal = renewsTotal;
    }

    public int getRenewsUsed() {
        return renewsUsed;
    }

    public void setRenewsUsed(int renewsUsed) {
        this.renewsUsed = renewsUsed;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
