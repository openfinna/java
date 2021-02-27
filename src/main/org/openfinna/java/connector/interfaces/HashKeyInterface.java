package org.openfinna.java.connector.interfaces;

public interface HashKeyInterface {

    void onFetchHashToken(String hashToken);

    void onError(Exception e);
}
