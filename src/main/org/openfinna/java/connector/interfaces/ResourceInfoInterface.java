package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.ResourceInfo;

public interface ResourceInfoInterface {
    void onResourceInfo(ResourceInfo resourceInfo);

    void onError(Exception e);
}
