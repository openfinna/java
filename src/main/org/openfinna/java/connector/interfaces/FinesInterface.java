package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.fines.Fines;

public interface FinesInterface {
    void onFines(Fines fines);

    void onError(Exception e);
}
