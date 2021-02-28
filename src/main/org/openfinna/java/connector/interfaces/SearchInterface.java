package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.ResourceInfo;

import java.util.List;

public interface SearchInterface {
    void onSearchResults(int totalCount, List<ResourceInfo> resourceInfoList);

    void onError(Exception e);
}
