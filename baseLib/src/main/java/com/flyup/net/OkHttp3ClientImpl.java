package com.flyup.net;

import com.flyup.common.utils.CollectionUtils;
import com.flyup.common.utils.LogUtil;
import com.flyup.common.utils.ThreadManager;
import com.flyup.common.utils.UIUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by focux on 2016-3-22 .
 */
public class OkHttp3ClientImpl implements HttpClient {
    private static final String TAG = "OkHttpClientImpl";

    private static Map<String,ApiInterceptor> interceptors = new ConcurrentHashMap<>();

    private static final long DEFAULT_CONN_TIMEOUT = 90;

    private static final OkHttpClient mClient = new OkHttpClient.Builder().
            writeTimeout(DEFAULT_CONN_TIMEOUT, TimeUnit.SECONDS).
            readTimeout(DEFAULT_CONN_TIMEOUT, TimeUnit.SECONDS).
            connectTimeout(DEFAULT_CONN_TIMEOUT, TimeUnit.SECONDS).
            addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    onRequestIntercept(request);

                    Response response = chain.proceed(request);
                    onResponseIntercept(response);
                    return response;
                }
            }).build();


    private static void onResponseIntercept(final Response response) {
        ThreadManager.getLongPool().execute(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, ApiInterceptor> entry : interceptors.entrySet()) {
                    entry.getValue().onResponse(response);
                }
            }
        });
    }


    private static void onRequestIntercept(final Request request) {
        ThreadManager.getLongPool().execute(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, ApiInterceptor> entry : interceptors.entrySet()) {
                    entry.getValue().onRequest(request);
                }
            }
        });
    }


    /**
     * 同步
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static Response execute(Request request) throws IOException {
        return mClient.newCall(request).execute();
    }

    /**
     * 异步
     *
     * @param request
     * @param callback
     */
    public static void executeAsync(Request request, Callback callback) {
        mClient.newCall(request).enqueue(callback);
    }


    private static Request buildPostFormRequest(String url, String tag, Map<String, Object> paramMap) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .tag(tag);

        FormBody.Builder bodyBuilder = new FormBody.Builder();
        addFormParams(paramMap, bodyBuilder);
        return builder.post(bodyBuilder.build()).build();
    }

    private static void addFormParams(Map<String, Object> paramMap, FormBody.Builder bodyBuilder) {
        if (paramMap == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            bodyBuilder.add(key, value == null ? "" : value.toString());
        }
    }

    private static void addFormParams(Map<String, Object> paramMap, MultipartBody.Builder bodyBuilder) {
        if (paramMap == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            bodyBuilder.addPart(MultipartBody.Part.createFormData(key, value == null ? "" : value.toString()));
        }

    }


//    public static String createRequestBody(Map<String, Object> params) {
//        String jsonParams = null;
//        if (params != null) {
//
//            jsonParams = new JSONObject(params).toString();
//        }
//        return putPlatformInfo(jsonParams);
//    }

    /**
     * post请求
     * body 为json形式
     *
     * @param url      地址
     * @param postBody json格式的字符串
     * @param tag      标识该请求，可用于以后取消
     * @return 返回的字符串
     * @throws IOException
     */
    public static String postJson(String url, String postBody, String tag) throws IOException {
        Request req = buildPostJsonRequest(url, postBody, tag);
        Response resp = execute(req);
        if (resp.isSuccessful()) {
            String respStr = resp.body().string();
            resp.body().close();
            return respStr;
        } else {
            throw new IOException("Unexpected code : " + resp);
        }
    }

    /**
     * @param url
     * @param postBody body为JSON
     * @param tag
     * @param callback
     * @throws IOException
     */
    public static void postJsonInbackground(String url, String postBody, String tag, Callback callback) throws IOException {

        Request req = buildPostJsonRequest(url, postBody, tag);
        executeAsync(req, callback);
    }

    private static Request buildPostJsonRequest(String url, String postBody, String tag) {
        Request.Builder builder = new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json"), postBody))
                .url(url)
                .tag(tag);

        return builder.build();
    }

    @Override
    public String get(String url, String tag, Map<String, Object> params) throws IOException {
        Request req = new Request.Builder()
                .get()
                .url(url)
                .tag(tag)
                .build();
        Response resp = execute(req);
        if (resp.isSuccessful()) {
            String respStr = resp.body().string();
            resp.body().close();
            return respStr;
        } else {
            throw new IOException("Unexpected code : " + resp);
        }
    }


    @Override
    public void getInbackground(String url, final String tag, Map<String, Object> params, final NetWorkCallBack<String> callback) {
        onStart(tag, callback);

        url = HttpUtil.concatUrl(url, params, HttpConstants.DEFAULT_CHARSET);
        Request req = new Request.Builder()
                .get()
                .url(url)
                .tag(tag)
                .build();
        executeAsync(req, new StringCallbackImpl(callback, tag));
    }

    @Override
    public String postForm(String url, String tag, Map<String, Object> params) throws IOException {
        //url = HttpUtil.concatUrl(url, params, HttpConstants.DEFAULT_CHARSET);

        Request req = buildPostFormRequest(url, tag, params);
        Response resp = execute(req);
        if (resp.isSuccessful()) {
            String respStr = resp.body().string();
            resp.body().close();
            return respStr;
        } else {
            throw new IOException("Unexpected code : " + resp);
        }
    }

    @Override
    public void postFormInbackground(String url, final String tag, Map<String, Object> params, final NetWorkCallBack<String> callback) {
        //表单形式参数一般在body里，业务需求拼接在url，应在业务封装中拼接
        //url = HttpUtil.concatUrl(url, params, HttpConstants.DEFAULT_CHARSET);
        onStart(tag, callback);

        Request req = buildPostFormRequest(url, tag, params);
        executeAsync(req, new StringCallbackImpl(callback, tag));
    }

    @Override
    public void uploadFile(String url, String tag, Map<String, Object> params, File file, NetWorkCallBack<String> callback) {
        //check file
        if (file == null || !file.exists() || !file.isFile()) {
            LogUtil.i(TAG, "upload fail file " + file);
            if (callback != null) {
                callback.onFailure(tag, new HttpException("upload file error"));
            }
            return;
        }

        onStart(tag, callback);

        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody);

        addFormParams(params, builder);

        Request req = new Request.Builder()
                .url(url)
                .tag(tag)
                .post(builder.build())
                .build();

        executeAsync(req, new StringCallbackImpl(callback, tag));

    }

    @Override
    public void uploadFiles(String url, String tag, Map<String, Object> params, List<String> files, NetWorkCallBack<String> callback) {

        //check file
        if (CollectionUtils.isEmpty(files)) {
            LogUtil.i(TAG, "upload fail files " + files);
            if (callback != null) {
                callback.onFailure(tag, new HttpException("upload files error"));
            }
            return;
        }


        List<File> fileList = new ArrayList<>();
        for (String f : files) {
            File file = new File(f);
            if (!file.exists() || !file.isFile()) {
                LogUtil.i(TAG, "upload fail files " + file);
                if (callback != null) {
                    callback.onFailure(tag, new HttpException("upload files error"));
                }
                return;
            }
            fileList.add(file);
        }

        //TODO
        //if (fileList.size() == 1) {
        //    uploadFile(url, tag, params, fileList.get(0), callback);
        //     return;
        // }
        onStart(tag, callback);

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        addFormParams(params, builder);

        for (int i = 0; i < fileList.size(); i++) {
            File file = fileList.get(i);
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
            builder.addFormDataPart("file" + i, file.getName(), fileBody);
        }


        Request req = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();

        executeAsync(req, new StringCallbackImpl(callback, tag));
    }

    @Override
    public String postBody(String url, String tag, String body) throws IOException {
        return postJson(url, body, tag);
    }

    @Override
    public void postBodyInbackground(String url, final String tag, String body, final NetWorkCallBack<String> callback) {
        onStart(tag, callback);

        Request req = buildPostJsonRequest(url, body, tag);
        executeAsync(req, new StringCallbackImpl(callback, tag));
    }

    @Override
    public void addInterceptor(String key,ApiInterceptor interceptor) {
        try {

            interceptors.put(key,interceptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class StringCallbackImpl implements Callback {

        NetWorkCallBack<String> callback;
        String tag;

        public StringCallbackImpl(NetWorkCallBack<String> requestCallback, String tag) {
            this.callback = requestCallback;
            this.tag = tag;
        }


        void failure(final HttpException e) {
            if (callback != null) {
                UIUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(tag, e);
                    }
                });
            }
        }

        @Override
        public void onFailure(Call call, IOException e) {
            failure(new HttpException(e));
        }

        @Override
        public void onResponse(Call call, Response response) {
            LogUtil.i(tag, "onResponse ：" + response);

            boolean successful = false;
            try {
                if (response.isSuccessful()) {
                    final String result = response.body().string();//string()只能调用一次，否则发生java.lang.IllegalStateException: closed
                    LogUtil.i(tag, "onResponse " + tag + "---body>> " + result);
                    if (callback != null) {
                        UIUtils.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(tag, result);
                            }
                        });
                    }
                    successful = true;
                }

                if (!successful) {
                    failure(new HttpException(response.code()));
                }
            } catch (Exception e) {
                LogUtil.e(e);
                failure(new HttpException(e));
            } finally {
                try {
                    response.body().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void onStart(final String tag, final NetWorkCallBack callback) {
        if (callback != null) {
//            UIUtils.post(new Runnable() {
//                @Override
//                public void run() {
            callback.onStart(tag);
//                }
//            });
        }
    }

    @Override
    public void cancel(String tag) {
        //mClient.(tag);
    }
}