package org.openfinna.java.connector.classes.models.libraries.schedule;

import java.io.Serializable;
import java.util.Date;

public class Schedule implements Serializable {
    private Date opens;
    private Date closes;
    private boolean selfService;

    public Schedule(Date opens, Date closes, boolean selfService) {
        this.opens = opens;
        this.closes = closes;
        this.selfService = selfService;
    }

    public Date getOpens() {
        return opens;
    }

    public void setOpens(Date opens) {
        this.opens = opens;
    }

    public Date getCloses() {
        return closes;
    }

    public void setCloses(Date closes) {
        this.closes = closes;
    }

    public boolean isSelfService() {
        return selfService;
    }

    public void setSelfService(boolean selfService) {
        this.selfService = selfService;
    }
}
