package com.flyup.net.image;

import android.app.Activity;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.flyup.common.utils.LogUtil;
import com.flyup.common.utils.UIUtils;

import java.io.File;

/**
 * Created by focux on 2016-3-23 .
 */
public class ImageLoader {
    private static final String TAG = "ImageLoader";

//    /**
//     * 加载图片，只需配置ImageView标签的app:iamgeUrl属性即可
//     */
//    @BindingAdapter({"bind:imageUrl"})
//    public static void loadImage(ImageView view, String url) {
//        load(view, url, 0, 0, true);
//    }
//
//    /**
//     * 加载图片，只需配置ImageView标签的app:iamgeUrl属性即可
//     */
//    @BindingAdapter({"bind:imageUrlCircle"})
//    public static void loadCircleImage(ImageView view, String url) {
//
//        loadCircle(view, url, 0, true);
//    }
//
//    /**
//     * 加载图片，只需配置ImageView标签的app:iamgeUrl属性即可
//     */
//    @BindingAdapter({"bind:imageUrl", "bind:round"})
//    public static void loadImage(ImageView view, String url, int round) {
//        load(view, url, round, 0, false);
//    }


    public static void load(ImageView view, String url) {
        load(view, url, 0, 0, false);
    }
    public static void load(ImageView view, String url, int defaultResId) {
        load(view, url, 0, defaultResId, false);
    }
   public static void load(ImageView view, String url, int defaultResId, int errorResId) {
        load(view, url, 0, defaultResId,  errorResId,false);
    }



    public static void load(ImageView view, String url, int roundInDP, int defaultResId, boolean centerCrop) {
         load(view,url,roundInDP,defaultResId,0,centerCrop);
    }
    /**
     * 一般加载图片
     *
     * @param view
     * @param url
     * @param roundInDP        图片显示圆角，默认0，不带圆角,单位dp
     * @param defaultResId
     * @param errorResId        出错图
     */
    public static void load(ImageView view, String url, int roundInDP, int defaultResId,int errorResId, boolean centerCrop) {

        DrawableTypeRequest<String> request = getRequestManager().load(url);
        if (defaultResId > 0) {
            request.placeholder(defaultResId);
        }
        if (errorResId > 0) {
            request.error(errorResId);
        }
        if (centerCrop) {
            request.centerCrop();
        }
        if (roundInDP > 0) {
            request.transform(new GlideRoundTransform(view.getContext(), roundInDP));
        }

        request.into(view);
    }


    /**
     * 加载 gif 图
     *
     * @param view
     * @param resId
     */
    public static void loadGif(ImageView view, int resId) {
        Glide.with(UIUtils.getContext()).load(resId).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).centerCrop().into(view);

    }

    private static RequestManager manager;

    private static RequestManager getRequestManager() {
        if (manager == null) {
            manager = Glide.with(UIUtils.getContext());
        }
        return manager;
    }


    /**
     * 加载圆形图片
     *
     * @param view
     * @param url          图片地址
     */
    public static void loadCircle(ImageView view, String url) {
        loadCircle(view,url,0,true);
    }
    /**
     * 加载圆形图片
     *
     * @param view
     * @param url          图片地址
     * @param defaultResId 占位图
     * @param centerCrop   图片居中裁剪，默认false
     */
    public static void loadCircle(ImageView view, String url, int defaultResId, boolean centerCrop) {
        LogUtil.i(TAG, "loadCircle URL >> " + url);
        DrawableTypeRequest<String> request = getRequestManager().load(url);

        if (defaultResId > 0) {
            request.placeholder(defaultResId);
        }
        if (centerCrop) {
            request.centerCrop();
        }

        request.crossFade();
        request.transform(new GlideCircleTransform(view.getContext()));
        request.into(view);
    }

    /**
     * 加载圆形图片   本地图片
     */
    public static void loadCircle(ImageView view, int resId) {
        DrawableTypeRequest<Integer> request = getRequestManager().load(resId);

        request.transform(new GlideCircleTransform(view.getContext()));
        request.into(view);
    }


    public static void loadBitmap(Activity activity, Target<File> target, String url) {

        DrawableTypeRequest<String> request = getRequestManager().load(url);
        request.downloadOnly(target);
    }


}
