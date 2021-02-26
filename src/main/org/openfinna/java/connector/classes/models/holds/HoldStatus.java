package org.openfinna.java.connector.classes.models.holds;

public enum HoldStatus {
    WAITING(0),
    IN_TRANSIT(1),
    AVAILABLE(2);

    HoldStatus(int status) {
    }
}
