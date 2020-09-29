package org.openkirkes.java.connector.interfaces;

import org.openkirkes.java.connector.classes.models.User;

public interface AccountDetailsInterface {
    void onGetAccountDetails(User user);

    void onError(Exception e);
}
