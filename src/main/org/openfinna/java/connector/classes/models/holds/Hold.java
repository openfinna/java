package org.openfinna.java.connector.classes.models.holds;

import org.openfinna.java.connector.classes.models.Resource;

import java.util.Date;

public class Hold {
    private String id;
    private String actionId;
    private HoldStatus holdStatus;
    private boolean cancellable;
    private HoldPickupData holdPickupData;
    private int queue;
    private Date expirationDate;
    private Date holdDate;
    private Resource resource;

    public Hold(String id, String actionId, HoldStatus holdStatus, boolean cancellable, HoldPickupData holdPickupData, int queue, Date expirationDate, Date holdDate, Resource resource) {
        this.id = id;
        this.actionId = actionId;
        this.holdStatus = holdStatus;
        this.cancellable = cancellable;
        this.holdPickupData = holdPickupData;
        this.queue = queue;
        this.expirationDate = expirationDate;
        this.holdDate = holdDate;
        this.resource = resource;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public HoldStatus getHoldStatus() {
        return holdStatus;
    }

    public void setHoldStatus(HoldStatus holdStatus) {
        this.holdStatus = holdStatus;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    public HoldPickupData getHoldPickupData() {
        return holdPickupData;
    }

    public void setHoldPickupData(HoldPickupData holdPickupData) {
        this.holdPickupData = holdPickupData;
    }

    public int getQueue() {
        return queue;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Date getHoldDate() {
        return holdDate;
    }

    public void setHoldDate(Date holdDate) {
        this.holdDate = holdDate;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
