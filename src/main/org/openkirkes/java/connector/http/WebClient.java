package org.openkirkes.java.connector.http;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.openkirkes.java.connector.utils.WebClientCookieJar;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class WebClient {

    private final WebClientCookieJar clientCookieJar = new WebClientCookieJar();
    private String defaultLanguage = "en-gb";
    private String kirkesBaseURL = "https://kirkes.finna.fi";
    private OkHttpClient client, nonSessionClient;

    public WebClient() {
        initWebClient(clientCookieJar, null);
    }

    public WebClient(String language) {
        this.defaultLanguage = language;
        initWebClient(clientCookieJar, null);
    }

    public WebClient(Cookie cookie) {
        clientCookieJar.addCookie(cookie);
        initWebClient(clientCookieJar, null);
    }

    public WebClient(Cookie cookie, String language) {
        this.defaultLanguage = language;
        clientCookieJar.addCookie(cookie);
        initWebClient(clientCookieJar, null);
    }

    public WebClient(Cookie cookie, Cache cache) {
        clientCookieJar.addCookie(cookie);
        initWebClient(clientCookieJar, cache);
    }

    public WebClient(Cookie cookie, Cache cache, String language) {
        this.defaultLanguage = language;
        clientCookieJar.addCookie(cookie);
        initWebClient(clientCookieJar, cache);
    }

    public WebClient(Cache cache) {
        initWebClient(clientCookieJar, cache);
    }


    public WebClient(Cache cache, String language) {
        this.defaultLanguage = language;
        initWebClient(clientCookieJar, cache);
    }

    private static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    private static String getProtocol(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.getScheme();
    }

    private static String optimizeURL(String url) {
        try {
            return getProtocol(url) + "://" + getDomainName(url);
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }
    }

    public static String generateURL(String url, String path) {
        try {
            return getProtocol(url) + "://" + getDomainName(url) + "/" + path;
        } catch (Exception e) {
            e.printStackTrace();
            return url + "/" + path;
        }
    }

    public void setKirkesBaseURL(String kirkesBaseURL) {
        this.kirkesBaseURL = kirkesBaseURL;
    }

    private Headers getDefaultHeaders() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Referer", optimizeURL(kirkesBaseURL));
        headerMap.put("Origin", optimizeURL(kirkesBaseURL) + "/");
        headerMap.put("User-Agent", "Mozilla/5.0");
        return Headers.of(headerMap);
    }

    private void initWebClient(CookieJar cookieJar, Cache cache) {
        // Adding language cookie
        try {
            clientCookieJar.addCookie(new Cookie.Builder().domain(getDomainName(kirkesBaseURL)).expiresAt(-1).httpOnly().secure().path("/").name("language").value(defaultLanguage).build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Interceptor interceptor = chain -> {
            Request request = chain.request();
            // try the request
            Response response = chain.proceed(request);
            int tryCount = 0;
            while (!response.isSuccessful() && tryCount < 5) {
                tryCount++;
                // retry the request
                response = chain.proceed(request);
            }
            // otherwise just pass the original response on
            return response;
        };
        client = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .cache(cache)
                .cookieJar(cookieJar)
                .addInterceptor(interceptor).build();
        nonSessionClient = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .cache(cache)
                .addInterceptor(interceptor).build();
    }

    /**
     * GET Request without caching
     *
     * @param session           Is session (cookie jar) used or not
     * @param url               URL
     * @param webClientListener Interface
     */
    public void getRequest(boolean session, String url, WebClientListener webClientListener) {
        Request request = new Request.Builder()
                .url(url)
                .headers(getDefaultHeaders())
                .cacheControl(new CacheControl.Builder().noCache().build())
                .get().build();
        getSuitableClientAndRequest(session, webClientListener, request);
    }

    /*
     * Requests start here
     */

    /**
     * POST Request without caching
     *
     * @param session           Is session (cookie jar) used or not
     * @param url               URL
     * @param requestBody       Request Body of POST Request
     * @param webClientListener Interface
     */
    public void postRequest(boolean session, String url, RequestBody requestBody, WebClientListener webClientListener) {
        Request request = new Request.Builder()
                .url(url)
                .headers(getDefaultHeaders())
                .cacheControl(new CacheControl.Builder().noCache().build())
                .post(requestBody).build();
        getSuitableClientAndRequest(session, webClientListener, request);
    }

    /**
     * GET Request with caching
     *
     * @param session           Is session (cookie jar) used or not
     * @param url               URL
     * @param webClientListener Interface
     */
    public void getRequestWithCache(boolean session, String url, WebClientListener webClientListener) {
        Request request = new Request.Builder()
                .url(url)
                .headers(getDefaultHeaders())
                //.cacheControl(new CacheControl.Builder().noCache().build())
                .get().build();
        getSuitableClientAndRequest(session, webClientListener, request);
    }

    private void getSuitableClientAndRequest(boolean session, WebClientListener webClientListener, Request request) {
        OkHttpClient httpClient = (session) ? client : nonSessionClient;
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                webClientListener.onFailed(call, e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                webClientListener.onResponse(response);
            }
        });
    }

    /**
     * onFailed = OkHttp HTTP client error
     */
    public interface WebClientListener {
        void onFailed(@NotNull Call call, @NotNull IOException e);

        void onResponse(@NotNull Response response);
    }
}
