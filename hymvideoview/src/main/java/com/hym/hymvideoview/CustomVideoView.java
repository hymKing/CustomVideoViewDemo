package com.hym.hymvideoview;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.flyup.download.DownloadInfo;
import com.flyup.download.DownloadManager;
import com.flyup.download.DownloadState;
import com.flyup.net.image.ImageLoader;

/**
 * Desc:
 * <p>自定义的视频播放组件:
 * <br>①视频播放功能
 * <br>②视频播放的进度条
 * <br>③视频播放过程事件的拦截回调
 * <p/>
 * Created by Hym on 2016/5/3 20:11.
 * <br>Modified by zlx 2016/5/20
 * <br>Modification：imgbtnPlay pbplay方法暴露  修改 R.layout.layout_custom_videoview 布局
 * <p/>
 * Created by Hym on 2016/05/27
 * 视频组件待完善的功能：
 * <br>①未播放状态：不显示进度条 已修复
 * <br>②未播放状态，设置视频源的时候，显示预览图，无预览图，显示默认图片 已修复
 * <br>③拦截事件的定制化:添加定制拦截事件接口 setCallBack(VideoCallBack callBack,int interceptTime)
 * <p/>
 * <p/>
 * Created by Hym on 2016/05/30
 * 视频组件待完善的功能：
 * <br>①添加停止播放，恢复默认状态
 * <br>②修复遇到视频播放拦截以后，停止播放
 * <p/>
 * <p/>
 * modified by Hym on 2016/05/31
 * 视频组件待完善的功能：
 * <br>当视频在播放状态的时候，停止过后，设置新的路径，视频自动播放的bug的修复
 * <br>setVideoPath()中做了canPause判断，canPause是true的时候，停止之前的播放，再设置新的path</>
 * <p/>
 * <p/>
 * modified by Hym on 2016/06/01
 * 视频组件待完善的功能：
 * <br>修复预览图拉升问题
 * <br>添加点击屏幕暂停的方法
 * <p/>
 *
 */
//TODO:暴露播放暂停的回调
//TODO:添加进度条的显示和隐藏


public class CustomVideoView extends LinearLayout implements  HymVideoView.VideoViewCallback{
    private boolean debug = true;
    private Context mContext;
    HymVideoView hvVideo;
    ImageView preHvImg;
    ProgressBar pbplay;
    HymMediaController mMediaController;
    int currentTime = 0;
    /**视频播放源的路径*/
    String mPath;
    /**
     * 默认拦截事件
     */
    int interceptTime = 2000;
    /**
     * 处理视频播放进度条的handler
     */
    final Handler handler = new Handler();
    /**
     * 处理视频进度条的runable
     */
    Runnable runnable = new Runnable() {
        public void run() {
            int duration = hvVideo.getCurrentPosition();
            pbplay.setMax(hvVideo.getDuration());
            pbplay.setProgress(duration);
            //LogUtil.e("VideoTest", "duration:" + duration + "");
            currentTime += 100;
            if (currentTime > interceptTime && mVICallBack != null) {
                hvVideo.pause();
                mVICallBack.onIntercept();
            } else {
                handler.postDelayed(runnable, 100);
            }
        }
    };

    public CustomVideoView(Context context) {
        super(context);
        init(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_custom_videoview, this, true);
        mContext = context;
        hvVideo = (HymVideoView) findViewById(R.id.hv_video);
        preHvImg = (ImageView) findViewById(R.id.hv_pre_img);
        pbplay = (ProgressBar) findViewById(R.id.pb_play);
        mMediaController = (HymMediaController) findViewById(R.id.media_controller);
        hvVideo.setMediaController(mMediaController);
        initCVV();
    }

    boolean preparedFlag = false;

    /**
     * 初始化播放，进度条等相关逻辑
     */
    private void initCVV() {
        setPbProgressVisibility(View.GONE);
        hvVideo.setVideoViewCallback(this);
        hvVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                final int currentPosition = hvVideo.getDuration();
                if (pbplay != null) {
                    pbplay.setMax(currentPosition);
                }
                currentTime=0;
                setPbProgressVisibility(View.GONE);
                preparedFlag = true;
                if (debug)
                    Log.e("VideoTest", "onPrapare");
            }
        });
        hvVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                handler.removeCallbacks(runnable);
                pbplay.setMax(0);
                currentTime = 0;
                setPbProgressVisibility(View.GONE);
                if (debug)
                    Log.e("VideoTest", "setOnCompletionListener");
                preHvImg.setVisibility(View.GONE);
            }
        });
        hvVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                preHvImg.setVisibility(View.VISIBLE);
                Toast.makeText(mContext, "视频资源异常", Toast.LENGTH_SHORT).show();
                if (debug)
                    Log.e("VideoTest", "setOnErrorListener");
                return false;
            }
        });
    }


    int mCurrentPosition = 0;

    /**
     * activity的 onPause方法中调用，保存当前视频播放的状态
     */
    public void onActivityOnPause() {
        mCurrentPosition = hvVideo.getCurrentPosition();
    }

    /**
     * activity的onRestart方法中调用，恢复视频之前的播放状态
     */
    public void onActivityOnRestart() {
        try {
            if (hvVideo != null) {
                if (mCurrentPosition != 0) {
                    hvVideo.seekTo(mCurrentPosition);
                    hvVideo.start();
                    mCurrentPosition = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置预览图片的资源
     *
     * @param id
     */
    public void setPreImg(int id) {
        preHvImg.setBackgroundResource(id);
    }

    public ImageView getPreUvimg() {
        return preHvImg;
    }

    /**
     * 设置预览图片的显示
     *
     * @param visibility
     */
    private void setPreImgVisibility(int visibility) {
        preHvImg.setVisibility(visibility);
    }


    /**
     * 设置进度条的显示和隐藏
     * @param visibility
     */
    public void setPbProgressVisibility(int visibility){
        pbplay.setVisibility(visibility);
    }

    /**
     * 设置视频的播放路径
     *
     * @param path
     */
    public void setVideoPath(String path) {
        hvVideo.setVideoPath(path);

    }

    /**
     *
     *
     * @param firstFrameUrl
     */
    public void setVideoFirstFrame(String firstFrameUrl) {
        if (!TextUtils.isEmpty(firstFrameUrl)) {
            ImageLoader.load(preHvImg, firstFrameUrl,0, R.mipmap.ic_launcher,true);
        }

    }

    //处理下载视频的处理
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (debug)
                        Log.e("VideoTest", "下载后：uvVideo.setVideoPath(mPath)" + mPath);
                    if (hvVideo.canPause()) {
                        hvVideo.pause();
                    }
                    hvVideo.setVideoPath(mPath);
            }
            super.handleMessage(msg);
        }
    };

    public void download(final String urlPath) {
        final DownloadManager manager = DownloadManager.getInstance();
        manager.registerObserver(new DownloadManager.DownloadObserver() {
            @Override
            public void onDownloadStateChanged(final DownloadInfo info) {
                if (info.getUrl().equals(urlPath)) {
                    switch (info.getDownloadState()) {
                        case DownloadState.STATE_ERROR:
                            manager.unRegisterObserver(this);
                            break;
                        case DownloadState.STATE_DOWNLOADED:
                            manager.unRegisterObserver(this);
                            mPath = info.getPath();
                            myHandler.sendEmptyMessage(0);
                            break;
                    }
                }
            }

            @Override
            public void onDownloadProgressed(DownloadInfo info) {
            }

        });
        manager.download(urlPath);
    }

    /**
     * 视频开始
     */
    public void start() {
        hvVideo.start();
    }

    /**
     * 视频暂停
     */
    public void pause() {
        hvVideo.pause();
    }


    /**
     * 设置视频准备完成的回调
     * @param l
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        hvVideo.setOnPreparedListener(l);
    }

    /**
     * 设置视频被播放完成的回调
     * @param l
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        hvVideo.setOnCompletionListener(l);
    }

    /**
     * 设置视频播放发生错误的回调
     *
     * @param l
     */
    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
        hvVideo.setOnErrorListener(l);
    }

    /**
     * 视频操作过程中的相关回调的全局变量
     */
    public VideoInterceptCallBack mVICallBack;
    /**
     * 视频拦截回调接口
     */
    public interface VideoInterceptCallBack {
        void onIntercept();
    }
    /**
     * 设置视频过程中的相关回调
     * @param callBack
     */
    public void setOnVideoInterceptCallBack(VideoInterceptCallBack callBack) {
        mVICallBack = callBack;
        this.interceptTime = 2000;
    }

    /**
     * 设置视频过程中的相关回调,定制拦截事件
     * @param callBack
     */
    public void setOnVideoInterceptCallBack(VideoInterceptCallBack callBack, int interceptTime) {
        mVICallBack = callBack;
        this.interceptTime = interceptTime;
    }



    private ExtendVideoViewCallBack mExtendVideoViewCallBack;
    /**设置外部组件的回调接口，比如在activity中，要获得，播放开始，停止，全屏等的变化状态。*/
    public interface ExtendVideoViewCallBack{
        void onScaleChange(boolean isFullscreen);
        void onPause(final MediaPlayer mediaPlayer);
        void onStart(final MediaPlayer mediaPlayer);
        void onBufferingStart(final MediaPlayer mediaPlayer);
        void onBufferingEnd(final MediaPlayer mediaPlayer);
    }
    public void setOnExtendVideoViewCallBack(ExtendVideoViewCallBack extendVideoViewCallBack){
        mExtendVideoViewCallBack=extendVideoViewCallBack;
    }
    @Override
    public void onScaleChange(boolean isFullscreen) {
        if(mExtendVideoViewCallBack!=null){
            mExtendVideoViewCallBack.onScaleChange(isFullscreen);
        }
    }

    @Override
    public void onPause(MediaPlayer mediaPlayer) {
        if(mExtendVideoViewCallBack!=null){
            mExtendVideoViewCallBack.onPause(mediaPlayer);
        }
    }

    @Override
    public void onStart(MediaPlayer mediaPlayer) {
        setPreImgVisibility(View.GONE);
        if(mExtendVideoViewCallBack!=null){
            mExtendVideoViewCallBack.onStart(mediaPlayer);
        }
    }

    @Override
    public void onBufferingStart(MediaPlayer mediaPlayer) {
        if(mExtendVideoViewCallBack!=null){
            mExtendVideoViewCallBack.onBufferingStart(mediaPlayer);
        }
    }

    @Override
    public void onBufferingEnd(MediaPlayer mediaPlayer) {
        if(mExtendVideoViewCallBack!=null){
            mExtendVideoViewCallBack.onBufferingEnd(mediaPlayer);
        }
    }
}
