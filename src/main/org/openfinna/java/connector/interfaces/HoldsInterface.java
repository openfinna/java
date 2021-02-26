package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.holds.Hold;

import java.util.List;

public interface HoldsInterface {
    void onGetHolds(List<Hold> holds);

    void onError(Exception e);
}
