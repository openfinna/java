package org.openkirkes.java.connector.interfaces;

import okhttp3.Call;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface LoginCSRFInterface {

    void onFetchCSRFToken(String csrfToken);

    void onFailed(Call call, @NotNull IOException e);

    void onError(Exception e);
}
