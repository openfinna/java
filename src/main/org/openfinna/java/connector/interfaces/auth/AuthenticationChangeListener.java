package org.openfinna.java.connector.interfaces.auth;

import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.building.Building;

public interface AuthenticationChangeListener {
    void onAuthenticationChange(UserAuthentication userAuthentication, User user, Building building);
}
