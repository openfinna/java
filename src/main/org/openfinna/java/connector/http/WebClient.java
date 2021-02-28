package org.openfinna.java.connector.http;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.openfinna.java.connector.utils.WebClientCookieJar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class WebClient {

    private final WebClientCookieJar clientCookieJar = new WebClientCookieJar();
    private String defaultLanguage = "en-gb";
    public static String kirkesBaseURL = "https://finna.fi";
    public static String finnaBaseUrl = "https://api.finna.fi";
    private OkHttpClient client, nonSessionClient, clientNoRed, getNonSessionClientNoRed;
    private final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    public WebClient() {
        initWebClient(clientCookieJar, null);
    }

    public WebClientCookieJar getClientCookieJar() {
        return clientCookieJar;
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

    private Proxy getDebugProxy() {
        int port = 8888;
        return new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress("127.0.0.1", port));
    }

    private static String getProtocol(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.getScheme();
    }

    public static String optimizeURL(String url) {
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

    public String generateApiURL(String path) {
        try {
            return getProtocol(finnaBaseUrl) + "://" + getDomainName(finnaBaseUrl) + "/" + path;
        } catch (Exception e) {
            e.printStackTrace();
            return finnaBaseUrl + "/" + path;
        }
    }

    public void setKirkesBaseURL(String kirkesBaseURL) {
        WebClient.kirkesBaseURL = kirkesBaseURL;
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

           /* final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext;
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };*/

            client = new OkHttpClient.Builder()
                    .callTimeout(10, TimeUnit.SECONDS)
                    .cache(cache)
                    .cookieJar(cookieJar)
                    /*  .proxy(getDebugProxy())
                      .sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
                      .hostnameVerifier(hostnameVerifier)*/

                    //.addInterceptor(loggingInterceptor)
                    .build();
            nonSessionClient = new OkHttpClient.Builder()
                    .callTimeout(10, TimeUnit.SECONDS)
                    .cache(cache)
                    /* .proxy(getDebugProxy())
                     .sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
                     .hostnameVerifier(hostnameVerifier)*/

                    //.addInterceptor(loggingInterceptor)
                    .build();

            clientNoRed = new OkHttpClient.Builder()
                    .callTimeout(10, TimeUnit.SECONDS)
                    .cache(cache)
                    .followSslRedirects(false)
                    .followRedirects(false)
                    .cookieJar(cookieJar)
                    /* .proxy(getDebugProxy())
                     .sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
                     .hostnameVerifier(hostnameVerifier)*/

                    //.addInterceptor(loggingInterceptor)
                    .build();
            getNonSessionClientNoRed = new OkHttpClient.Builder()
                    .callTimeout(10, TimeUnit.SECONDS)
                    .followSslRedirects(false)
                    .followRedirects(false)
                    .cache(cache)
                    /*.proxy(getDebugProxy())
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
                    .hostnameVerifier(hostnameVerifier)*/

                    //.addInterceptor(loggingInterceptor)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }


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
