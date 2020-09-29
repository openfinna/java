package org.openkirkes.java.connector.interfaces;

import org.openkirkes.java.connector.classes.models.User;

public interface LoginInterface {
    void onError(Exception e);

    void onLogin(User user);
}
