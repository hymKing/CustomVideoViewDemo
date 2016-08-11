package com.flyup.net;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by DongSheng on 2016-3-23.
 */
public interface HttpClient {

    /**
     * get请求
     *
     * @param url 地址
     * @param tag 标识该请求，可用于以后取消
     * @return 返回的字符串
     * @throws IOException
     */
    String get(String url, String tag, Map<String, Object> params) throws IOException;

    /**
     * @param url
     * @param tag
     * @param params
     * @param callback 回调可能在子线程中执行
     * @throws IOException
     */
    void getInbackground(String url, String tag, Map<String, Object> params, NetWorkCallBack<String> callback);

    /**
     * post请求
     * 表单方式提交
     *
     * @param url    地址
     * @param tag    标识该请求，可用于以后取消
     * @param params
     * @return 返回的字符串
     * @throws IOException
     */
    String postForm(String url, String tag, Map<String, Object> params) throws IOException;

    /**
     * 表单方式提交
     *
     * @param url
     * @param tag
     * @param params
     * @param callback 回调可能在子线程中执行
     * @throws IOException
     */
    void postFormInbackground(String url, String tag, Map<String, Object> params, NetWorkCallBack<String> callback);

    /***
     * 上传文件
     * @param url
     * @param tag
     * @param params
     * @param file
     * @param callback
     * @throws IOException
     */
    void uploadFile(String url, String tag, Map<String, Object> params, File file, NetWorkCallBack<String> callback);

    void uploadFiles(String url, String tag, Map<String, Object> params, List<String> files, NetWorkCallBack<String> callback);


    /**
     * post请求
     *
     * @param url  地址
     * @param tag  标识该请求，可用于以后取消
     * @param body 提交内容
     * @return 返回的字符串
     * @throws IOException
     */
    String postBody(String url, String tag, String body) throws IOException;

    void postBodyInbackground(String url, String tag, String body, NetWorkCallBack<String> callback);



    /**
     * 添加拦截器
     * 在发出请求前和响应数据后回调
     * @param key           拦截器唯一标识
     * @param interceptor
     */
    void addInterceptor(String key,ApiInterceptor interceptor);

    /**
     * 取消一个请求
     *
     * @param tag
     */
    void cancel(String tag);
}
