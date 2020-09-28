package org.openkirkes.java.connector;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.openkirkes.java.connector.exceptions.InvalidCredentialsException;
import org.openkirkes.java.connector.exceptions.KirkesClientException;
import org.openkirkes.java.connector.http.WebClient;
import org.openkirkes.java.connector.interfaces.LoginCSRFInterface;
import org.openkirkes.java.connector.interfaces.LoginInterface;
import org.openkirkes.java.connector.parser.KirkesHTMLParser;

import java.io.IOException;

public class KirkesClient {

    private final WebClient webClient;

    public KirkesClient() {
        webClient = new WebClient();
    }

    public KirkesClient(WebClient webClient) {
        this.webClient = webClient;
    }


    public void login(String username, String password, boolean fetchUserDetails, LoginInterface loginInterface) {
        fetchLoginCSRF(new LoginCSRFInterface() {
            @Override
            public void onFetchCSRFToken(String csrfToken) {
                // TODO login
                System.out.println(csrfToken);
                MultipartBody postData = new MultipartBody.Builder()
                        .addFormDataPart("username", username)
                        .addFormDataPart("password", password)
                        .addFormDataPart("target", "kirkes")
                        .addFormDataPart("auth_method", "MultiILS")
                        .addFormDataPart("layout", "lightbox")
                        .addFormDataPart("csrf", csrfToken)
                        .addFormDataPart("processLogin", "Kirjaudu")
                        .addFormDataPart("secondary_username", "").build();
                //webClient.postRequest(true, true, webClient.generateURL( "MyResearch/Home?layout=lightbox&lbreferer=https%3A%2F%2F" + webClient.getDomainName() + "%2FMyResearch%2FUserLogin"),
                webClient.postRequest(true, true, webClient.generateURL("MyResearch/Home?layout=lightbox&lbreferer=https://kirkes.finna.fi/MyResearch/UserLogin"),
                        postData, new WebClient.WebClientListener() {
                            @Override
                            public void onFailed(@NotNull Call call, @NotNull IOException e) {
                                loginInterface.onError(e);
                            }

                            @Override
                            public void onResponse(@NotNull Response response) {
                                System.out.println("RESPCODE: " + response.code());
                                if (response.code() == 205) {
                                    if (fetchUserDetails) {
                                        // TODO fetch and then notify about successful login
                                    } else {
                                        loginInterface.onLogin();
                                    }
                                } else {
                                    try {
                                        System.out.println("CONTENT: " + response.body().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    loginInterface.onError(new InvalidCredentialsException());
                                }
                            }
                        });
            }

            @Override
            public void onFailed(Call call, @NotNull IOException e) {
                loginInterface.onError(e);
            }

            @Override
            public void onError(Exception e) {
                loginInterface.onError(e);
            }
        });
    }

    private void fetchLoginCSRF(LoginCSRFInterface loginCSRFInterface) {
        webClient.getRequest(true, true, webClient.generateURL("MyResearch/UserLogin?layout=lightbox"), new WebClient.WebClientListener() {
            @Override
            public void onFailed(@NotNull Call call, @NotNull IOException e) {
                loginCSRFInterface.onFailed(call, e);
            }

            @Override
            public void onResponse(@NotNull Response response) {
                if (response.code() == 200) {
                    try {
                        String csrfToken = KirkesHTMLParser.parseCSRF(response.body().string());
                        if (csrfToken != null)
                            loginCSRFInterface.onFetchCSRFToken(csrfToken);
                        else
                            loginCSRFInterface.onError(new KirkesClientException("Unable to find CSRF token"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        loginCSRFInterface.onFailed(null, e);
                    } catch (Exception e) {
                        e.printStackTrace();
                        loginCSRFInterface.onError(e);
                    }
                } else {
                    loginCSRFInterface.onError(new KirkesClientException("Unable to find CSRF token"));
                }
            }
        });
    }
}
