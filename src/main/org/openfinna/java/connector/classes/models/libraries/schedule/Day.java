package org.openfinna.java.connector.classes.models.libraries.schedule;

import java.util.Date;

public class Day {
    private Date date;
    private boolean closed;
    private Schedule schedule;

    public Day(Date date, boolean closed, Schedule schedule) {
        this.date = date;
        this.closed = closed;
        this.schedule = schedule;
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
}

