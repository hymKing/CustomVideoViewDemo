package com.flyup.common.utils;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * 广播工具类
 * Created by Focux on 2016-3-31.
 */
public class BroadCastManager {
    private LocalBroadcastManager mBroadcastManager;

    private BroadCastManager() {
        mBroadcastManager = LocalBroadcastManager.getInstance(UIUtils.getContext());
    }

    static BroadCastManager mManager;

    public static synchronized BroadCastManager getInstance() {
        if (mManager == null) {
            mManager = new BroadCastManager();
        }
        return mManager;
    }



    public void unregisterLocalReceiver(BroadcastReceiver receiver) {
        mBroadcastManager.unregisterReceiver(receiver);
    }

    public void registerLocalReceiver(IntentFilter filter, BroadcastReceiver receiver) {
        mBroadcastManager.registerReceiver(receiver, filter);
    }

    public void registerLocalReceiver(String action, BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter(action);
        mBroadcastManager.registerReceiver(receiver, filter);
    }


    /**
     * 同步广播
     * 所有相应的receiver都接到广播后才相应
     *
     * @param intent
     */
    public void sendBroadcastSync(Intent intent) {
        mBroadcastManager.sendBroadcastSync(intent);
    }

    public void sendBroadcastSync(String action) {
        sendBroadcastSync(new Intent(action));
    }

    /**
     * 异步广播
     * receiver一接到广播就响应
     *
     * @param intent
     */
    public void sendBroadcastAsync(Intent intent) {
        mBroadcastManager.sendBroadcast(intent);
    }


    /**
     * receiver一接到广播就响应
     *
     * @param action
     */
    public void sendBroadcastAsync(String action) {
        sendBroadcastAsync(new Intent(action));
    }

}
