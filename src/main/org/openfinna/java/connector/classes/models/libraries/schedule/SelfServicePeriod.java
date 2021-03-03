package org.openfinna.java.connector.classes.models.libraries.schedule;

import java.util.Date;

public class SelfServicePeriod {
    private Date start;
    private Date end;

    public SelfServicePeriod(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
