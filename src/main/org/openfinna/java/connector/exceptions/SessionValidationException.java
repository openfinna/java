package org.openfinna.java.connector.exceptions;

public class SessionValidationException extends KirkesClientException {
    public SessionValidationException() {
        super("Unable to log in to kirkes");
    }
}
