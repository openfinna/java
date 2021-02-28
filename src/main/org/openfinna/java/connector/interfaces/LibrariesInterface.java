package org.openfinna.java.connector.interfaces;

import org.openfinna.java.connector.classes.models.libraries.Library;

import java.util.List;

public interface LibrariesInterface {
    void onGetLibraries(List<Library> libraries);

    void onGetLibrary(Library library);

    void onError(Exception e);
}
