package com.hym.hymvideoview.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

/**
 * Created by hym on 2016/12/19.
 */

public class HttpUtil {
    private static ConnectivityManager mConnectivityManager;

    public static ConnectivityManager getConnectivityManager(Context context) {
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return mConnectivityManager;
    }
    public static String getNetworkType(Context context) {

        String netTypeMode = "";
        try {
            final NetworkInfo mNetworkInfo = getConnectivityManager(context).getActiveNetworkInfo();
            if (mNetworkInfo == null) {
                return "";
            }
            final int netType = mNetworkInfo.getType();
            if (netType == ConnectivityManager.TYPE_WIFI) {
                // wifi上网
                netTypeMode = "wifi";
            } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                // 接入点上网
                final String netMode = mNetworkInfo.getExtraInfo();
                if (!TextUtils.isEmpty(netMode)) {
                    return netMode;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return netTypeMode;
    }
}
