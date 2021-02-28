package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.User;

public interface LoginInterface {
    void onError(Exception e);

    void onLogin(User user);
}
