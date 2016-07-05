package com.flyup.net;

/**
 * Created by Administrator on 2016/5/4.
 */
public class DefaultCallBack implements NetWorkCallBack {
    @Override
    public boolean onStart(String tag) {
        return false;
    }

    @Override
    public boolean onSuccess(String tag, Object response) {
        return false;
    }

    @Override
    public boolean onFailure(String tag, HttpException e) {
        return false;
    }

    @Override
    public boolean onLoading(String apiName, long total, long current, boolean isUploading) {
        return false;
    }
}
