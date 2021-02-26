package org.openfinna.java;

import org.openfinna.java.connector.FinnaClient;

public class OpenFinna {
    public static FinnaClient newClient() {
        return new FinnaClient();
    }
}
