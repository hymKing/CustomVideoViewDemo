package com.flyup.net;

/**
 * Created by solo on 15/8/13.
 */
public interface NetWorkCallBack<T> {

    boolean onStart(String tag);

    boolean onSuccess(String tag, T response);

    boolean onFailure(String tag, HttpException e);

    boolean onLoading(String apiName, long total, long current,
                      boolean isUploading);
}
