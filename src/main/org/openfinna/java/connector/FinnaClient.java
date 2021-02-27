package org.openfinna.java.connector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.Resource;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.UserType;
import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.holds.HoldingDetails;
import org.openfinna.java.connector.classes.models.holds.PickupLocation;
import org.openfinna.java.connector.classes.models.loans.Loan;
import org.openfinna.java.connector.exceptions.InvalidCredentialsException;
import org.openfinna.java.connector.exceptions.KirkesClientException;
import org.openfinna.java.connector.exceptions.SessionValidationException;
import org.openfinna.java.connector.http.WebClient;
import org.openfinna.java.connector.interfaces.*;
import org.openfinna.java.connector.parser.KirkesHTMLParser;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FinnaClient {

    private final WebClient webClient;
    private UserAuthentication userAuthentication;

    public FinnaClient() {
        webClient = new WebClient();
    }

    public FinnaClient(WebClient webClient) {
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
                        .add("target", userAuthentication.getUserType().getId())
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

    public void renewLoan(Loan loan, LoansInterface loansInterface) {
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                FormBody postData = new FormBody.Builder()
                        .add("selectAllIDS[]", loan.getRenewId())
                        .add("renewAllIDS[]", loan.getRenewId())
                        .add("renewSelectedIDS[]", loan.getRenewId())
                        .add("renewSelected", "this should not be empty, at least it's working :D").build();
                webClient.postRequest(true, true, webClient.generateURL("MyResearch/CheckedOut"), postData, new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        loansInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                loansInterface.onLoanRenew(loan, KirkesHTMLParser.checkRenewResult(response.body().string(), loan));
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

    public void getHolds(HoldsInterface holdsInterface) {
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                webClient.getRequest(true, true, webClient.generateURL("MyResearch/Holds"), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        holdsInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                holdsInterface.onGetHolds(KirkesHTMLParser.parseHolds(response.body().string()));
                            } catch (Exception e) {
                                holdsInterface.onError(e);
                            }
                        } else {
                            holdsInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                holdsInterface.onError(e);
            }
        });
    }

    public void changeHoldPickupLocation(Hold hold, String locationId, HoldsInterface holdsInterface) {
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                webClient.getRequest(true, true, webClient.generateURL(String.format("AJAX/JSON?method=%s&requestId=%s&pickupLocationId=%s", "changePickupLocation", hold.getActionId(), locationId)), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        holdsInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                String body = Objects.requireNonNull(response.body()).string();
                                if (isJSONValid(body)) {
                                    JSONObject object = new JSONObject(body).optJSONObject("data");
                                    if (object != null) {
                                        if (object.optBoolean("success", false))
                                            holdsInterface.onChangePickupLocation(hold);
                                        else
                                            throw new KirkesClientException("Finna error: " + object.optString("sysMessage", "Unknown"));
                                    } else
                                        throw new KirkesClientException("Malformed JSON: " + body);
                                } else
                                    throw new KirkesClientException("Unable to parse JSON: " + body);
                            } catch (IOException e) {
                                holdsInterface.onError(e);
                            }
                        } else {
                            holdsInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                holdsInterface.onError(e);
            }
        });
    }

    public void getPickupLocations(Resource resource, PickupLocationsInterface pickupLocationsInterface, String type) {
        if (type == null)
            type = "0";
        String finalType = type;
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                webClient.getRequest(true, true, webClient.generateURL(String.format("AJAX/JSON?method=%s&id=%s&requestGroupId=%s", "getRequestGroupPickupLocations", resource.getId(), finalType)), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        pickupLocationsInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                String body = Objects.requireNonNull(response.body()).string();
                                if (isJSONValid(body)) {
                                    JSONObject object = new JSONObject(body);
                                    List<PickupLocation> pickupLocations = new Gson().fromJson(object.optJSONObject("data").optJSONArray("locations").toString(), TypeToken.getParameterized(List.class, PickupLocation.class).getType());
                                    // Fetching additional details
                                    fetchHashKey(resource.getId(), new HashKeyInterface() {
                                        @Override
                                        public void onFetchHashToken(String hashToken) {
                                            webClient.getRequest(true, true, webClient.generateURL(String.format("Record/%s/Hold?id=%s&level=title&hashKey=%s&layout=lightbox#tabnav", resource.getId(), resource.getId(), hashToken)), new WebClient.WebClientListener() {
                                                @Override
                                                public void onFailed(@NotNull Call call, @NotNull IOException e) {
                                                    pickupLocationsInterface.onError(e);
                                                }

                                                @Override
                                                public void onResponse(@NotNull Response response) {
                                                    try {
                                                        HoldingDetails holdingDetails = KirkesHTMLParser.extractHoldingDetails(response.body().string());
                                                        getDefaultPickupLocation(new PickupLocationsInterface() {
                                                            @Override
                                                            public void onFetchPickupLocations(List<PickupLocation> locations, HoldingDetails holdingDetails, PickupLocation defaultLocation) {

                                                            }

                                                            @Override
                                                            public void onFetchDefaultPickupLocation(PickupLocation defaultLocation, List<PickupLocation> allLocations) {
                                                                pickupLocationsInterface.onFetchPickupLocations(pickupLocations, holdingDetails, defaultLocation);
                                                            }

                                                            @Override
                                                            public void onError(Exception e) {
                                                                pickupLocationsInterface.onError(e);
                                                            }
                                                        });
                                                    } catch (IOException e) {
                                                        pickupLocationsInterface.onError(e);
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            pickupLocationsInterface.onError(e);
                                        }
                                    });
                                } else
                                    throw new KirkesClientException("Unable to parse JSON: " + body);
                            } catch (IOException e) {
                                pickupLocationsInterface.onError(e);
                            }
                        } else {
                            pickupLocationsInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                pickupLocationsInterface.onError(e);
            }
        });
    }

    public void getDefaultPickupLocation(PickupLocationsInterface holdsInterface) {
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                webClient.getRequest(true, true, webClient.generateURL("MyResearch/Profile"), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        holdsInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                String html = response.body().string();
                                holdsInterface.onFetchDefaultPickupLocation(KirkesHTMLParser.getHomeLibrary(html), KirkesHTMLParser.getHomeLibraries(html));
                            } catch (Exception e) {
                                holdsInterface.onError(e);
                            }
                        } else {
                            holdsInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                holdsInterface.onError(e);
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

    public void getUserTypes(UserTypeInterface userTypeInterface) {
        webClient.getRequest(true, true, webClient.generateURL("MyResearch/UserLogin?layout=lightbox"), new WebClient.WebClientListener() {
            @Override
            public void onFailed(@NotNull Call call, @NotNull IOException e) {
                userTypeInterface.onError(e);
            }

            @Override
            public void onResponse(@NotNull Response response) {
                if (response.code() == 200) {
                    try {
                        List<UserType> userTypes = KirkesHTMLParser.parseUserTypes(Objects.requireNonNull(response.body()).string());
                        userTypeInterface.onUserTypes(userTypes);
                    } catch (Exception e) {
                        userTypeInterface.onError(e);
                    }
                } else {
                    userTypeInterface.onError(new KirkesClientException("Unsuccessful request: " + response.code()));
                }
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
                login(FinnaClient.this.userAuthentication, false, new LoginInterface() {
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

    private void fetchHashKey(String id, HashKeyInterface hashKeyInterface) {
        FormBody body = new FormBody.Builder()
                .add("tab", "holdings").build();
        webClient.postRequest(true, true, webClient.generateURL("Record/" + id + "/AjaxTab"), body, new WebClient.WebClientListener() {
            @Override
            public void onFailed(@NotNull Call call, @NotNull IOException e) {
                hashKeyInterface.onError(e);
            }

            @Override
            public void onResponse(@NotNull Response response) {
                if (response.code() == 200) {
                    try {
                        String hashKey = KirkesHTMLParser.extractHashKey(response.body().string());
                        if (hashKey != null)
                            hashKeyInterface.onFetchHashToken(hashKey);
                        else
                            hashKeyInterface.onError(new KirkesClientException("Unable to find hashToken"));
                    } catch (Exception e) {
                        hashKeyInterface.onError(e);
                    }
                } else {
                    hashKeyInterface.onError(new KirkesClientException("Unable to find CSRF token"));
                }
            }
        });
    }

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ignored) {
            try {
                new JSONArray(test);
            } catch (JSONException ignored2) {
                return false;
            }
        }
        return true;
    }


}
