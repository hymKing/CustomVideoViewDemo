package com.flyup.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * 应用的配置文件
 * <p/>
 * 公有的一些配置信息
 * Created by focux on 2016-3-30 .
 */

public class PreferenceUtil {

    private static Editor getEditor() {
        return getPreferences().edit();
    }

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(UIUtils.getContext());
    }

    public static SharedPreferences getPreferences(String name) {
        return UIUtils.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }


    public static void saveString(String key, String value) {
        Editor editor = getEditor();
        editor.putString(key, value);
        editor.apply();
    }

    public static void remove(String key) {
        Editor editor = getEditor();
        editor.remove(key);
        editor.apply();
    }
    public static void remove(String preferenceName,String key) {
        Editor editor = getPreferences(preferenceName).edit();
        editor.remove(key);
        editor.apply();
    }


    public static void saveString(String preferenceName, String key, String value) {
        Editor editor = getPreferences(preferenceName).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key) {
        return getPreferences().getString(key, "");
    }

    public static String getString(String preferenceName, String key) {
        return getPreferences(preferenceName).getString(key, "");
    }

    public static Set<String> getStringSet(String key) {
        return getPreferences().getStringSet(key, new HashSet<String>());
    }

    public static void saveStringSet(String key, Set<String> values) {
        Editor editor = getEditor();
        editor.putStringSet(key, values);
        editor.apply();
    }

    public static void saveBoolean(String key, boolean value) {
        Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.apply();
    }


    public static void saveBoolean(String preferenceName, String key, boolean value) {
        Editor editor = getPreferences(preferenceName).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }


    public static boolean getBoolean(String key) {
        return getPreferences().getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getPreferences().getBoolean(key, defaultValue);
    }

    public static boolean getBoolean(String preferenceName, String key, boolean defaultValue) {
        return getPreferences(preferenceName).getBoolean(key, defaultValue);
    }



    public static long getLong(String key) {
        return getPreferences().getLong(key, 0l);
    }
    public static long getLong(String preferenceName,String key) {
        return getPreferences(preferenceName).getLong(key, 0l);
    }

    public static void saveLong(String key, long value) {
        Editor editor = getEditor();
        editor.putLong(key, value);
        editor.apply();
    }
    public static void saveLong(String preferenceName,String key, long value) {
        SharedPreferences preferences = getPreferences(preferenceName);
        Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void saveInt(String preferenceName, String key, int value) {
        Editor editor = getPreferences(preferenceName).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void saveInt(String key, int value) {
        Editor editor = getEditor();
        editor.putInt(key, value);
        editor.apply();
    }
    public static int getInt(String key) {
        return getPreferences().getInt(key, 0);
    }

    public static int getInt(String key, int defaultValue) {
        return getPreferences().getInt(key, defaultValue);
    }

    public static int getInt(String preferenceName, String key, int defaultValue) {
        return getPreferences(preferenceName).getInt(key, defaultValue);
    }

    public static int getInt(String preferenceName, String key) {
        return getPreferences(preferenceName).getInt(key, 0);
    }

    /**
     * 自增
     *
     * @param preferenceName
     * @param key
     * @param intervel       步长
     * @return
     */
    public static boolean increment(String preferenceName, String key, int intervel) {
        SharedPreferences p = null;
        if (preferenceName == null) {
            p = getPreferences();
        } else {
            p = getPreferences(preferenceName);
        }
        int old = p.getInt(key, 0);
        Editor editor = p.edit();
        editor.putInt(key, old + intervel);
        return editor.commit();
    }

    /**
     * 自增
     *
     * @param key
     * @param intervel 步长
     * @return
     */
    public static boolean increment(String key, int intervel) {

        return increment(null, key, intervel);
    }


    public static void clear(String name) {
        Editor edit = getPreferences(name).edit();
        edit.clear().commit();
    }


}
