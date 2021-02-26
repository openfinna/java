package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.User;

public interface AccountDetailsInterface {
    void onGetAccountDetails(User user);

    void onError(Exception e);
}
