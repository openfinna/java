package org.openfinna.java.connector.classes.models.libraries.schedule;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Day implements Serializable {
    private Date date;
    private boolean closed;
    private Schedule schedule;
    private List<SelfServicePeriod> selfServicePeriods;

    public Day(Date date, boolean closed, Schedule schedule, List<SelfServicePeriod> selfServicePeriods) {
        this.date = date;
        this.closed = closed;
        this.schedule = schedule;
        this.selfServicePeriods = selfServicePeriods;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public List<SelfServicePeriod> getSelfServicePeriods() {
        return selfServicePeriods;
    }

    public void setSelfServicePeriods(List<SelfServicePeriod> selfServicePeriods) {
        this.selfServicePeriods = selfServicePeriods;
    }
}

