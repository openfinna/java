package org.openkirkes.java.connector.interfaces;

public interface LoginInterface {
    void onError(Exception e);

    void onLogin();
}
