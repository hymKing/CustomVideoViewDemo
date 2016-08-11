package com.flyup.ui;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.app.FragmentActivity;


/**
 * Created by focuz on 2016-3-22 .
 */
public class LibraryActivity extends FragmentActivity {

    protected final String TAG = this.getClass().getSimpleName();
    /** 记录处于前台的Activity */
    private static LibraryActivity mForegroundActivity = null;

    public <T extends ViewDataBinding> T bindView(int layoutResID) {
        return DataBindingUtil.setContentView(this, layoutResID);
    }

    @Override
    protected void onResume() {
        mForegroundActivity = this;

        super.onResume();
    }


    @Override
    protected void onPause() {
        mForegroundActivity = null;
        super.onPause();
    }


    /** 获取当前处于前台的activity */
    public static LibraryActivity getForegroundActivity() {
        return mForegroundActivity;
    }


}
