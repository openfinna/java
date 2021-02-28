package org.openfinna.java.connector.classes.models.holds;

import java.io.Serializable;

public enum HoldStatus implements Serializable {
    WAITING(0),
    IN_TRANSIT(1),
    AVAILABLE(2);

    HoldStatus(int status) {
    }
}
