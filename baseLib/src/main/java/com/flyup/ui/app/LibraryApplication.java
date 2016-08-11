package com.flyup.ui.app;

import android.app.Application;

import com.flyup.BuildConfig;

/**
 * Created by focux on 2016-3-23 .
 */
public class LibraryApplication extends Application {
    protected final String TAG = this.getClass().getSimpleName();
    private static LibraryApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;


        if (BuildConfig.DEBUG) {
            //com.facebook.stetho.Stetho.initializeWithDefaults(this);
        }
    }

    /**
     * 获取整个应用的上下文
     */
    public synchronized static <T extends LibraryApplication> T getInstance() {
        return (T) instance;
    }


}
