package org.openfinna.java.connector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.Resource;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.UserType;
import org.openfinna.java.connector.classes.models.building.Building;
import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.holds.HoldingDetails;
import org.openfinna.java.connector.classes.models.holds.PickupLocation;
import org.openfinna.java.connector.classes.models.loans.Loan;
import org.openfinna.java.connector.exceptions.InvalidCredentialsException;
import org.openfinna.java.connector.exceptions.KirkesClientException;
import org.openfinna.java.connector.exceptions.SessionValidationException;
import org.openfinna.java.connector.http.WebClient;
import org.openfinna.java.connector.interfaces.*;
import org.openfinna.java.connector.parser.FinnaJSONParser;
import org.openfinna.java.connector.parser.KirkesHTMLParser;
import org.openfinna.java.connector.utils.BuildingUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FinnaClient {

    private final WebClient webClient;
    private UserAuthentication userAuthentication;
    public static final String[] recordKeys = new String[]{"id", "title", "subTitle", "shortTitle", "cleanIsbn", "edition", "manufacturer", "year", "physicalDescription", "placesOfPublication", "subjects", "generalNotes", "languages", "originalLanguages", "publishers", "awards", "classifications", "authors", "formats"};
    // Cached values
    private Building cachedBuilding = null;

    public FinnaClient() {
        webClient = new WebClient();
    }

    public FinnaClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void changeUserAuthentication(UserAuthentication userAuthentication, boolean fetchUserDetails, LoginInterface loginInterface) {
        webClient.getClientCookieJar().clear();
        cachedBuilding = null;
        login(userAuthentication, fetchUserDetails, loginInterface);
    }

    public void login(UserAuthentication userAuthentication, boolean fetchUserDetails, LoginInterface loginInterface) {
        this.userAuthentication = userAuthentication;
        cachedBuilding = null;
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

    /**
     * Get Buildings, from HTML
     *
     * @param libraryChainInterface LibraryChainInterface
     */
    public void getBuildings(LibraryChainInterface libraryChainInterface) {
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                webClient.getRequest(true, true, webClient.generateURL("Content/organisations"), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        libraryChainInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                String html = response.body().string();
                                libraryChainInterface.onFetchLibraryBuildings(KirkesHTMLParser.getBuildings(html));
                            } catch (Exception e) {
                                libraryChainInterface.onError(e);
                            }
                        } else {
                            libraryChainInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                libraryChainInterface.onError(e);
            }
        });
    }

    /**
     * Get Buildings via AJAX
     * NOTE: This request is slow, totally about 1-2 seconds
     *
     * @param libraryChainInterface LibraryChainInterface
     */
    public void getBuildingsViaAjax(LibraryChainInterface libraryChainInterface) {
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                webClient.getRequest(true, true, webClient.generateURL("AJAX/JSON?method=getSideFacets&enabledFacets[]=building"), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        libraryChainInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                String body = Objects.requireNonNull(response.body()).string();
                                if (isJSONValid(body)) {
                                    JSONObject object = new JSONObject(body);
                                    List<Building> buildings = new Gson().fromJson(object.optJSONObject("data").optJSONObject("facets").optJSONObject("building").optJSONArray("list").toString(), TypeToken.getParameterized(List.class, Building.class).getType());
                                    libraryChainInterface.onFetchLibraryBuildings(buildings);
                                } else
                                    throw new KirkesClientException("Unable to parse JSON: " + body);
                            } catch (IOException e) {
                                libraryChainInterface.onError(e);
                            }
                        } else {
                            libraryChainInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                libraryChainInterface.onError(e);
            }
        });
    }

    private void searchFunc(String query, Building building, boolean rawData, int page, int limit, SearchInterface searchInterface) {
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder();
                httpUrlBuilder.host("example.com").scheme("https");
                httpUrlBuilder.addQueryParameter("lookfor", query);
                httpUrlBuilder.addQueryParameter("limit", String.valueOf(limit));
                httpUrlBuilder.addQueryParameter("page", String.valueOf(page));
                httpUrlBuilder.addQueryParameter("filter", "~building:\"" + building.getId() + "\"");
                for (String param : recordKeys) {
                    httpUrlBuilder.addQueryParameter("field[]", param);
                }
                if (rawData)
                    httpUrlBuilder.addQueryParameter("field[]", "rawData");
                webClient.getRequest(false, true, webClient.generateApiURL("api/v1/search?" + httpUrlBuilder.build().encodedQuery()), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        searchInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                String body = Objects.requireNonNull(response.body()).string();
                                if (isJSONValid(body)) {
                                    JSONObject object = new JSONObject(body);
                                    if (object.optInt("resultCount") > 0) {
                                        searchInterface.onSearchResults(object.optInt("resultCount"), FinnaJSONParser.parseResourceInfos(object.optJSONArray("records")));
                                    } else
                                        searchInterface.onSearchResults(0, new ArrayList<>());
                                } else
                                    throw new KirkesClientException("Unable to parse JSON: " + body);
                            } catch (IOException e) {
                                searchInterface.onError(e);
                            }
                        } else {
                            searchInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                searchInterface.onError(e);
            }
        });
    }

    public void search(String query, SearchInterface searchInterface) {
        System.out.println("Cache status: " + (cachedBuilding != null));
        if (cachedBuilding != null) {
            searchFunc(query, cachedBuilding, false, 1, 10, searchInterface);
        } else {
            getDefaultBuilding(new LibraryChainInterface() {
                @Override
                public void onFetchDefaultLibraryBuilding(Building building) {
                    search(query, searchInterface);
                }

                @Override
                public void onFetchLibraryBuildings(List<Building> buildingList) {

                }

                @Override
                public void onError(Exception e) {
                    searchInterface.onError(e);
                }
            });
        }
    }

    public void search(String query, boolean rawData, SearchInterface searchInterface) {
        if (cachedBuilding != null) {
            searchFunc(query, cachedBuilding, rawData, 1, 10, searchInterface);
        } else {
            getDefaultBuilding(new LibraryChainInterface() {
                @Override
                public void onFetchDefaultLibraryBuilding(Building building) {
                    search(query, searchInterface);
                }

                @Override
                public void onFetchLibraryBuildings(List<Building> buildingList) {

                }

                @Override
                public void onError(Exception e) {
                    searchInterface.onError(e);
                }
            });
        }
    }

    public void search(String query, int page, SearchInterface searchInterface) {
        if (cachedBuilding != null) {
            searchFunc(query, cachedBuilding, false, page, 10, searchInterface);
        } else {
            getDefaultBuilding(new LibraryChainInterface() {
                @Override
                public void onFetchDefaultLibraryBuilding(Building building) {
                    search(query, page, searchInterface);
                }

                @Override
                public void onFetchLibraryBuildings(List<Building> buildingList) {

                }

                @Override
                public void onError(Exception e) {
                    searchInterface.onError(e);
                }
            });
        }
    }

    public void search(String query, int page, boolean rawData, SearchInterface searchInterface) {
        if (cachedBuilding != null) {
            searchFunc(query, cachedBuilding, rawData, page, 10, searchInterface);
        } else {
            getDefaultBuilding(new LibraryChainInterface() {
                @Override
                public void onFetchDefaultLibraryBuilding(Building building) {
                    search(query, page, searchInterface);
                }

                @Override
                public void onFetchLibraryBuildings(List<Building> buildingList) {

                }

                @Override
                public void onError(Exception e) {
                    searchInterface.onError(e);
                }
            });
        }
    }

    public void search(String query, int page, int limit, SearchInterface searchInterface) {
        if (cachedBuilding != null) {
            searchFunc(query, cachedBuilding, false, page, limit, searchInterface);
        } else {
            getDefaultBuilding(new LibraryChainInterface() {
                @Override
                public void onFetchDefaultLibraryBuilding(Building building) {
                    search(query, page, limit, searchInterface);
                }

                @Override
                public void onFetchLibraryBuildings(List<Building> buildingList) {

                }

                @Override
                public void onError(Exception e) {
                    searchInterface.onError(e);
                }
            });
        }
    }

    public void search(String query, int page, int limit, boolean rawData, SearchInterface searchInterface) {
        if (cachedBuilding != null) {
            searchFunc(query, cachedBuilding, rawData, page, limit, searchInterface);
        } else {
            getDefaultBuilding(new LibraryChainInterface() {
                @Override
                public void onFetchDefaultLibraryBuilding(Building building) {
                    search(query, page, limit, rawData, searchInterface);
                }

                @Override
                public void onFetchLibraryBuildings(List<Building> buildingList) {

                }

                @Override
                public void onError(Exception e) {
                    searchInterface.onError(e);
                }
            });
        }
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

    public void getDefaultBuilding(String cardId, LibraryChainInterface libraryChainInterface) {
        if (cachedBuilding != null) {
            libraryChainInterface.onFetchDefaultLibraryBuilding(cachedBuilding);
            return;
        }
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                webClient.getRequest(true, true, webClient.generateURL("LibraryCards/editCard/" + cardId), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        libraryChainInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                UserType chainDetails = KirkesHTMLParser.getActiveChain(response.body().string());
                                if (chainDetails != null) {
                                    getBuildings(new LibraryChainInterface() {
                                        @Override
                                        public void onFetchDefaultLibraryBuilding(Building building) {

                                        }

                                        @Override
                                        public void onFetchLibraryBuildings(List<Building> buildingList) {
                                            Building building = null;
                                            String optimizedName = BuildingUtils.optimizeName(chainDetails.getName(), chainDetails.getId());
                                            for (Building iBuilding : buildingList) {
                                                String buildingName = BuildingUtils.optimizeName(iBuilding.getName(), "");
                                                if (optimizedName.equals(buildingName)) {
                                                    building = iBuilding;
                                                    break;
                                                }
                                            }
                                            cachedBuilding = building;
                                            libraryChainInterface.onFetchDefaultLibraryBuilding(building);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            libraryChainInterface.onError(e);
                                        }
                                    });
                                } else
                                    throw new KirkesClientException("Chain not found in card");
                            } catch (Exception e) {
                                libraryChainInterface.onError(e);
                            }
                        } else {
                            libraryChainInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                libraryChainInterface.onError(e);
            }
        });
    }

    /**
     * Because Finna apparently is so lost inside that uses different IDs for basically same thing, we need to fetch this in order to search within the building
     *
     * @param libraryChainInterface LibraryChainInterface
     */
    public void getDefaultBuilding(LibraryChainInterface libraryChainInterface) {
        getSelectedCardId(new CardInterface() {
            @Override
            public void onFetchCurrentCardId(String cardId) {
                getDefaultBuilding(cardId, new LibraryChainInterface() {

                    @Override
                    public void onFetchDefaultLibraryBuilding(Building building) {
                        libraryChainInterface.onFetchDefaultLibraryBuilding(building);
                    }

                    @Override
                    public void onFetchLibraryBuildings(List<Building> buildingList) {

                    }

                    @Override
                    public void onError(Exception e) {
                        libraryChainInterface.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                libraryChainInterface.onError(e);
            }
        });
    }

    public void getSelectedCardId(CardInterface cardInterface) {
        preCheck(new PreCheckInterface() {
            @Override
            public void onPreCheck() {
                webClient.getRequest(true, true, webClient.generateURL("MyResearch/Profile"), new WebClient.WebClientListener() {
                    @Override
                    public void onFailed(@NotNull Call call, @NotNull IOException e) {
                        cardInterface.onError(e);
                    }

                    @Override
                    public void onResponse(@NotNull Response response) {
                        if (response.code() == 200) {
                            try {
                                cardInterface.onFetchCurrentCardId(KirkesHTMLParser.getCurrentCardId(response.body().string()));
                            } catch (Exception e) {
                                cardInterface.onError(e);
                            }
                        } else {
                            cardInterface.onError(new KirkesClientException("Response code " + response.code()));
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                cardInterface.onError(e);
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
                                String html = response.body().string();
                                // Caching default building
                                String cardId = KirkesHTMLParser.getCurrentCardId(html);
                                if (cardId != null) {
                                    getDefaultBuilding(cardId, new LibraryChainInterface() {
                                        @Override
                                        public void onFetchDefaultLibraryBuilding(Building building) {
                                            cachedBuilding = building;
                                            detailsInterface.onGetAccountDetails(KirkesHTMLParser.parseUserDetails(html, cachedBuilding));
                                        }

                                        @Override
                                        public void onFetchLibraryBuildings(List<Building> buildingList) {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            detailsInterface.onError(e);
                                        }
                                    });
                                } else
                                    detailsInterface.onGetAccountDetails(KirkesHTMLParser.parseUserDetails(html));
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
                else {
                    login(FinnaClient.this.userAuthentication, false, new LoginInterface() {
                        @Override
                        public void onError(Exception e) {
                            validationInterface.onError(new SessionValidationException());
                        }

                        @Override
                        public void onLogin(User user) {
                            validationInterface.onSessionValidated();
                        }
                    });

                }

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

    public void setCachedBuilding(Building cachedBuilding) {
        this.cachedBuilding = cachedBuilding;
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
