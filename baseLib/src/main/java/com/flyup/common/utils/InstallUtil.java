package com.flyup.common.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

/**
 * 安装工具类<br>
 * 提供安装apk相关功能的静态方法
 */
public class InstallUtil {

    /**
     * 安装APK的MimeType
     **/
    static final String MIMETYPE_APK = "application/vnd.android.package-archive";

    /**
     * 交给系统安装apk
     *
     * @param context
     * @param path    apk的路径
     */
    public static void installInSystem(Context context, String path) {
        Uri uri = Uri.fromFile(new File(path));
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, MIMETYPE_APK);
        context.startActivity(intent);
    }
    /**
     * apk安装
     *
     * @param file
     * @param ctx
     */
    public static void apkInstaller(File file, Context ctx) {
        String permission = "777";
        try {
            String command = "chmod " + permission + " " + file.getPath();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        ctx.startActivity(intent);
    }


}