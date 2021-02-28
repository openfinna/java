package org.openfinna.java.connector.utils;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.openfinna.java.connector.http.WebClient;

import java.util.*;

public class WebClientCookieJar implements CookieJar {

    private final Set<Cookie> cookieStore = new LinkedHashSet<>();

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.addAll(cookies);
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> matchingCookies = new ArrayList<>();
        Iterator<Cookie> it = cookieStore.iterator();
        while (it.hasNext()) {
            Cookie cookie = it.next();
            if (cookie.expiresAt() < System.currentTimeMillis()) {
                it.remove();
            } else if (cookie.matches(url)) {
                matchingCookies.add(cookie);
            }
        }
        return matchingCookies;
    }

    public String getSession() {
        for (Cookie cookie : cookieStore) {
            if (cookie.matches(HttpUrl.get(WebClient.kirkesBaseURL))) {
                if (cookie.name().toLowerCase(Locale.ROOT).equals("phpsessid"))
                    return cookie.value();
            }
        }
        return null;
    }

    public void clear() {
        cookieStore.clear();
    }

    public void addCookie(Cookie cookie) {
        cookieStore.add(cookie);
    }
}