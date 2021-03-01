package org.openfinna.java.connector.interfaces;

public interface DescriptionInterface {
    void onGetDescription(String description);

    void onError(Exception e);
}
