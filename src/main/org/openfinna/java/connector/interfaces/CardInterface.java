package org.openfinna.java.connector.interfaces;

public interface CardInterface {
    void onFetchCurrentCardId(String cardId);

    void onError(Exception e);
}
