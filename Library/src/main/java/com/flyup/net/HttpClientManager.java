package com.flyup.net;

/**
 * Created by DongSheng on 2016-3-23.
 */
public class HttpClientManager {


    static volatile HttpClient httpClient;

    public synchronized static HttpClient getDefaultClient() {
        if (httpClient == null) {
            httpClient = new OkHttp3ClientImpl();
        }
        return httpClient;
    }
}
