package com.flyup.net;

import okhttp3.Request;
import okhttp3.Response;

/**
 * 网络拦截器
 *
 * Created by Focux on 2016-5-26.
 */
public abstract class ApiInterceptor  {

    protected static final String TAG = "ApiInterceptor";


    /**
     * 发出请求前回调
     * 可对请求做统一的处理 header修改等
     * @param request
     */
    protected void onRequest(Request request) {

    }

    /***
     * 请求响应后回调
     * @param response
     */
    protected void onResponse(Response response) {

    }


}
