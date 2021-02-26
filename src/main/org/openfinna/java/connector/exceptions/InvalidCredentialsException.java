package org.openfinna.java.connector.exceptions;

public class InvalidCredentialsException extends KirkesClientException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }

}
