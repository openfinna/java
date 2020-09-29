package org.openkirkes.java.connector.interfaces;

/**
 * Interface for validateSession request
 */
public interface SessionValidationInterface {
    void onSessionValidated();

    void onError(Exception e);
}
