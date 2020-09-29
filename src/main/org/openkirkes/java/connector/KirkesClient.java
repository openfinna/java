package org.openkirkes.java.connector;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.openkirkes.java.connector.classes.UserAuthentication;
import org.openkirkes.java.connector.classes.models.User;
import org.openkirkes.java.connector.exceptions.InvalidCredentialsException;
import org.openkirkes.java.connector.exceptions.KirkesClientException;
import org.openkirkes.java.connector.exceptions.SessionValidationException;
import org.openkirkes.java.connector.http.WebClient;
import org.openkirkes.java.connector.interfaces.*;
import org.openkirkes.java.connector.parser.KirkesHTMLParser;

import java.io.IOException;

public class KirkesClient {

    private final WebClient webClient;
    private UserAuthentication userAuthentication;

    public KirkesClient() {
        webClient = new WebClient();
    }

    public KirkesClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void changeUserAuthentication(UserAuthentication userAuthentication, boolean fetchUserDetails, LoginInterface loginInterface) {
        webClient.getClientCookieJar().clear();
        login(userAuthentication, fetchUserDetails, loginInterface);
    }

    public void login(UserAuthentication userAuthentication, boolean fetchUserDetails, LoginInterface loginInterface) {
        this.userAuthentication = userAuthentication;
        fetchLoginCSRF(new LoginCSRFInterface() {
            @Override
            public void onFetchCSRFToken(String csrfToken) {
                FormBody postData = new FormBody.Builder()
                        .add("username", userAuthentication.getUsername())
                        .add("password", userAuthentication.getPassword())
                        .add("target", "kirkes")
                        .add("auth_method", "MultiILS")
                        .add("layout", "lightbox")
                        .add("csrf", csrfToken)
                        .add("processLogin", "Kirjaudu")
                        .add("secondary_username", "").build();
                webClient.postRequest(true, true, webClient.generateURL("MyResearch/Home?layout=lightbox&lbreferer=https%3A%2F%2F" + webClient.getDomainName() + "%2FMyResearch%2FUserLogin"),
                        postData, new WebClient.WebClientListener() {
                            @Override
                            public void onFailed(@NotNull Call call, @NotNull IOException e) {
                                loginInterface.onError(e);
                            }

                            @Override
                            public void onResponse(@NotNull Response response) {
                                if (response.code() == 205) {
                                    if (fetchUserDetails) {
                                        getAccountDetails(new AccountDetailsInterface() {
                                            @Override
                                            public void onGetAccountDetails(User user) {
                                                loginInterface.onLogin(user);
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                loginInterface.onError(e);
                                            }
                                        });
                                    } else {
                                        loginInterface.onLogin(null);
                                    }
                                } else {
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

    public void getLoans(LoansInterface loansInterface) {
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                webClient.getRequest(true, true, webClient.generateURL("MyResearch/CheckedOut"), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        loansInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                loansInterface.onGetLoans(KirkesHTMLParser.parseLoans(response.body().string()));
                            } catch (IOException e) {
                                loansInterface.onError(e);
                            }
                        } else {
                            loansInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                loansInterface.onError(e);
            }
        });
    }

    public void getAccountDetails(AccountDetailsInterface detailsInterface) {
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                webClient.getRequest(true, true, webClient.generateURL("MyResearch/Profile"), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        detailsInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                detailsInterface.onGetAccountDetails(KirkesHTMLParser.parseUserDetails(response.body().string()));
                            } catch (IOException e) {
                                detailsInterface.onError(e);
                            }
                        } else {
                            detailsInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                detailsInterface.onError(e);
            }
        });
    }

    private void preCheck(PreCheckInterface preCheckInterface) {
        validateSession(new SessionValidationInterface() {
            @Override
            public void onSessionValidated() {
                preCheckInterface.onPreCheck();
            }

            @Override
            public void onError(Exception e) {
                login(KirkesClient.this.userAuthentication, false, new LoginInterface() {
                    @Override
                    public void onError(Exception e) {
                        preCheckInterface.onError(e);
                    }

                    @Override
                    public void onLogin(User user) {
                        validateSession(new SessionValidationInterface() {
                            @Override
                            public void onSessionValidated() {
                                preCheckInterface.onPreCheck();
                            }

                            @Override
                            public void onError(Exception e) {
                                preCheckInterface.onError(e);
                            }
                        });
                    }
                });
            }
        });
    }

    private void validateSession(SessionValidationInterface validationInterface) {
        webClient.getRequest(true, true, webClient.generateURL("AJAX/JSON?method=getUserTransactions"), new WebClient.WebClientListener() {
            @Override
            public void onFailed(@NotNull Call call, @NotNull IOException e) {
                validationInterface.onError(e);
            }

            @Override
            public void onResponse(@NotNull Response response) {
                if (response.isSuccessful())
                    validationInterface.onSessionValidated();
                else
                    validationInterface.onError(new SessionValidationException());
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
                        loginCSRFInterface.onFailed(null, e);
                    } catch (Exception e) {
                        loginCSRFInterface.onError(e);
                    }
                } else {
                    loginCSRFInterface.onError(new KirkesClientException("Unable to find CSRF token"));
                }
            }
        });
    }
}
