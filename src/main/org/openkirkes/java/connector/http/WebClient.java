package org.openkirkes.java.connector.http;

import com.google.gson.Gson;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.openkirkes.java.connector.utils.WebClientCookieJar;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class WebClient {

    private final WebClientCookieJar clientCookieJar = new WebClientCookieJar();
    private String defaultLanguage = "en-gb";
    private String kirkesBaseURL = "https://kirkes.finna.fi";
    private OkHttpClient client, nonSessionClient, clientNoRed, getNonSessionClientNoRed;

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

    public String getDomainName() {
        try {
            return getDomainName(kirkesBaseURL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return kirkesBaseURL;
        }
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

    public String generateURL(String path) {
        try {
            return getProtocol(kirkesBaseURL) + "://" + getDomainName(kirkesBaseURL) + "/" + path;
        } catch (Exception e) {
            e.printStackTrace();
            return kirkesBaseURL + "/" + path;
        }
    }

    public void setKirkesBaseURL(String kirkesBaseURL) {
        this.kirkesBaseURL = kirkesBaseURL;
    }

    private void appendDefaultHeaders(Request.Builder builder) {
        builder.addHeader("Referer", optimizeURL(kirkesBaseURL) + "/");
        builder.addHeader("Origin", optimizeURL(kirkesBaseURL) + "/");
        builder.addHeader("User-Agent", "Mozilla/5.0");
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

        clientNoRed = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .cache(cache)
                .followSslRedirects(false)
                .followRedirects(false)
                .cookieJar(cookieJar)
                .addInterceptor(interceptor).build();
        getNonSessionClientNoRed = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .followSslRedirects(false)
                .followRedirects(false)
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
    public void getRequest(boolean session, boolean redirect, String url, WebClientListener webClientListener) {
        Request.Builder request = new Request.Builder()
                .url(url)
                .cacheControl(new CacheControl.Builder().noCache().build())
                .get();
        appendDefaultHeaders(request);
        getSuitableClientAndRequest(session, redirect, webClientListener, request.build());
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
    public void postRequest(boolean session, boolean redirect, String url, RequestBody requestBody, WebClientListener webClientListener) {
        System.out.println(url);
        System.out.println(new Gson().toJson(clientCookieJar.getCookies()));
        Request.Builder request = new Request.Builder()
                .url(url)
                .method("POST", requestBody)
                .cacheControl(new CacheControl.Builder().noCache().build());
        appendDefaultHeaders(request);
        getSuitableClientAndRequest(session, redirect, webClientListener, request.build());
    }

    /**
     * GET Request with caching
     *
     * @param session           Is session (cookie jar) used or not
     * @param url               URL
     * @param webClientListener Interface
     */
    public void getRequestWithCache(boolean session, boolean redirect, String url, WebClientListener webClientListener) {
        Request.Builder request = new Request.Builder()
                .url(url)
                //.cacheControl(new CacheControl.Builder().noCache().build())
                .get();
        appendDefaultHeaders(request);
        getSuitableClientAndRequest(session, redirect, webClientListener, request.build());
    }

    private void getSuitableClientAndRequest(boolean session, boolean redirect, WebClientListener webClientListener, Request request) {
        OkHttpClient httpClient = (session) ? (redirect ? client : clientNoRed) : (redirect ? nonSessionClient : getNonSessionClientNoRed);
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
