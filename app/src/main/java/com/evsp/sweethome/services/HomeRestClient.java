package com.evsp.sweethome.services;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.entity.StringEntity;

public class HomeRestClient {
    private static final String BASE_URL = "http://shopen.com/open/partner/";
    private AsyncHttpClient client;
    private PersistentCookieStore cookieStore;

    public HomeRestClient(Context context) {
        client = new AsyncHttpClient();
        cookieStore = new PersistentCookieStore(context);
        client.setCookieStore(cookieStore);
    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public void postJson(String url, StringEntity se, JsonHttpResponseHandler responseHandler) {
        client.post(null, url, se, "application/json", responseHandler);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
