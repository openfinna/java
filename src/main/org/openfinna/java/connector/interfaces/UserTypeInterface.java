package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.UserType;

import java.util.List;

public interface UserTypeInterface {
    void onError(Exception e);

    void onUserTypes(List<UserType> userTypeList);
}
