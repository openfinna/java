package org.openkirkes.java.connector.interfaces;

public interface PreCheckInterface {
    void onPreCheck();

    void onError(Exception e);
}
