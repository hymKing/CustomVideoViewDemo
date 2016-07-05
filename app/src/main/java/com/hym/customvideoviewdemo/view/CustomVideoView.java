package com.hym.customvideoviewdemo.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.flyup.download.DownloadInfo;
import com.flyup.download.DownloadManager;
import com.flyup.download.DownloadState;
import com.flyup.net.image.ImageLoader;
import com.hym.customvideoviewdemo.R;

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
public class CustomVideoView/* extends LinearLayout */{
//    private boolean debug = true;
//    private Context mContext;
//    UniversalVideoView uvVideo;
//    ImageView preOnUvimg;
//    ImageButton imgbtnPlay;
//    ProgressBar pbplay;
//    int currentTime = 0;
//    /**视频播放源的路径*/
//    String mPath;
//    /**
//     * 默认拦截事件
//     */
//    int interceptTime = 2000;
//    /**
//     * 处理视频播放进度条的handler
//     */
//    final Handler handler = new Handler();
//    /**
//     * 处理视频进度条的runable
//     */
//    Runnable runnable = new Runnable() {
//        public void run() {
//            int duration = uvVideo.getCurrentPosition();
//            pbplay.setMax(uvVideo.getDuration());
//            setPbProgress(duration);
//            //LogUtil.e("VideoTest", "duration:" + duration + "");
//            currentTime += 100;
//            if (currentTime > interceptTime && mVCallBack != null) {
//                uvVideo.pause();
//                mVCallBack.onIntercept();
//            } else {
//                handler.postDelayed(runnable, 100);
//            }
//        }
//    };
//
//    public CustomVideoView(Context context) {
//        super(context);
//        init(context);
//    }
//
//    public CustomVideoView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init(context);
//    }
//
//    private void init(Context context) {
//        LayoutInflater.from(context).inflate(R.layout.layout_custom_videoview, this, true);
//        mContext = context;
//        uvVideo = (UniversalVideoView) findViewById(R.id.uv_video);
//        preOnUvimg = (ImageView) findViewById(R.id.pre_on_uv_img);
//        imgbtnPlay = (ImageButton) findViewById(R.id.imgbtn_play);
//        pbplay = (ProgressBar) findViewById(R.id.pb_play);
//        initCVV();
//    }
//
//    boolean preparedFlag = false;
//
//    /**
//     * 初始化播放，进度条等相关逻辑
//     */
//    private void initCVV() {
//        setPbProgressVisibility(View.GONE);
//        //pbplay.setVisibility(View.GONE);
//        uvVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                final int currentPosition = uvVideo.getDuration();
//                if (pbplay != null) {
//                    pbplay.setMax(currentPosition);
//                }
//                uvVideo.seekTo(1);
//                currentTime=0;
//                imgbtnPlay.setVisibility(View.VISIBLE);
//                setPbProgressVisibility(View.GONE);
//                //pbplay.setVisibility(View.GONE);
//                preparedFlag = true;
//                if (debug)
//                    Log.e("VideoTest", "onPrapare");
//            }
//        });
//        uvVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                handler.removeCallbacks(runnable);
//                if (imgbtnPlay.getVisibility() == View.INVISIBLE) {
//                    imgbtnPlay.setVisibility(View.VISIBLE);
//                }
//                setPbMax(0);
//                currentTime = 0;
//                //pbplay.setVisibility(View.GONE);
//                setPbProgressVisibility(View.GONE);
//                if (debug)
//                    Log.e("VideoTest", "setOnCompletionListener");
//            }
//        });
//        uvVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//            @Override
//            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
//                imgbtnPlay.setVisibility(View.VISIBLE);
//                setPbProgressVisibility(View.GONE);
//                pbplay.setVisibility(View.GONE);
//                //handler.removeCallbacks(runnable);
//                preOnUvimg.setVisibility(View.VISIBLE);
//                Toast.makeText(mContext, "视频资源异常", Toast.LENGTH_SHORT).show();
//                if (debug)
//                    Log.e("VideoTest", "setOnErrorListener");
//                return false;
//            }
//        });
//        imgbtnPlay.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                playVideo();
//            }
//        });
//        findViewById(R.id.id_video_layout).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (uvVideo.canPause()) {
//                    stopVideo();
//                } else {
//
//                }
//            }
//        });
//    }
//
//    /**
//     * 播放视频
//     */
//    public void playVideo() {
//        if (TextUtils.isEmpty(mPath) || !preparedFlag) {
//            return;
//        }
//        if (imgbtnPlay.getVisibility() == View.VISIBLE) {
//            imgbtnPlay.setVisibility(View.INVISIBLE);
//        }
//        if (preOnUvimg.getVisibility() == View.VISIBLE) {
//            preOnUvimg.setVisibility(View.GONE);
//        }
//        setPbProgressVisibility(View.VISIBLE);
//        //pbplay.setVisibility(View.VISIBLE);
//        handler.postDelayed(runnable, 0);
//        //播放视频
//        if (!TextUtils.isEmpty(mPath)) {
//            start();
//        } else {
//            Toast.makeText(mContext, "无播放视频源", Toast.LENGTH_LONG).show();
//            handler.removeCallbacks(runnable);
//            setPbProgressVisibility(View.GONE);
//            //pbplay.setVisibility(View.GONE);
//            imgbtnPlay.setVisibility(View.VISIBLE);
//            preOnUvimg.setVisibility(View.VISIBLE);
//        }
//    }
//
//    /**
//     * 停止视频播放
//     */
//    public void stopVideo() {
//        if (imgbtnPlay.getVisibility() != View.VISIBLE) {
//            imgbtnPlay.setVisibility(View.VISIBLE);
//        }
//        pause();
//    }
//
//    int mCurrentPosition = 0;
//
//    /**
//     * activity的 onPause方法中调用，保存当前视频播放的状态
//     */
//    public void onActivityOnPause() {
//        mCurrentPosition = uvVideo.getCurrentPosition();
//    }
//
//    /**
//     * activity的onRestart方法中调用，恢复视频之前的播放状态
//     */
//    public void onActivityOnRestart() {
//        try {
//            if (uvVideo != null) {
//                if (mCurrentPosition != 0) {
//                    uvVideo.seekTo(mCurrentPosition);
//                    uvVideo.start();
//                    mCurrentPosition = 0;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public ImageButton getImgbtnPlay() {
//        return imgbtnPlay;
//    }
//
//    public ProgressBar getPbplay() {
//        return pbplay;
//    }
//
//    /**
//     * 设置预览图片的资源
//     *
//     * @param id
//     */
//    public void setPreImg(int id) {
//        preOnUvimg.setBackgroundResource(id);
//    }
//
//    public ImageView getPreUvimg() {
//        return preOnUvimg;
//    }
//
//    /**
//     * 设置预览图片的显示
//     *
//     * @param visibility
//     */
//    public void setPreImgVisibility(int visibility) {
//        preOnUvimg.setVisibility(visibility);
//    }
//
//    /**
//     * 设置进度条的最大值
//     *
//     * @param max
//     */
//    public void setPbMax(int max) {
//        pbplay.setMax(max);
//    }
//
//    /**
//     * 设置进度条
//     *
//     * @param progress
//     */
//    public void setPbProgress(int progress) {
//        pbplay.setProgress(progress);
//    }
//
//    /**
//     * 设置进度条的显示和隐藏
//     * @param visibility
//     */
//    private void setPbProgressVisibility(int visibility){
//        pbplay.setVisibility(visibility);
//    }
//
//    /**
//     * 设置视频的播放路径
//     *
//     * @param path
//     */
//    public void setVideoPath(String path) {
//        if (uvVideo.canPause()) {
//            uvVideo.pause();
//        }
//        mPath = "";
//        if (path.startsWith("http")) {
//            download(path);
//        } else {
//            mPath = path;
//            if (debug)
//                Log.e("VideoTest", "本地：uvVideo.setVideoPath(mPath)" + mPath);
//            uvVideo.setVideoPath(path);
//        }
//
//    }
//
//    /**
//     * 设置视频的播放路径
//     *
//     * @param path
//     */
//    public void setVideoPathAndFirstFrame(String path, String firstFrameUrl) {
//        mPath = "";
//        if (path.startsWith("http")) {
//            download(path);
//        } else {
//            mPath = path;
//            if (debug)
//                Log.e("VideoTest", "本地：uvVideo.setVideoPath(mPath)" + mPath);
//            uvVideo.setVideoPath(path);
//        }
//        if (!TextUtils.isEmpty(firstFrameUrl)) {
//            //TODO:设置预览图
//            ImageLoader.load(preOnUvimg, firstFrameUrl,0, R.mipmap.ic_launcher,true);
//        }
//
//    }
//
//    //处理下载视频的处理
//    Handler myHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 0:
//                    if (debug)
//                        Log.e("VideoTest", "下载后：uvVideo.setVideoPath(mPath)" + mPath);
//                    if (uvVideo.canPause()) {
//                        uvVideo.pause();
//                    }
//                    uvVideo.setVideoPath(mPath);
//            }
//            super.handleMessage(msg);
//        }
//    };
//
//    public void download(final String urlPath) {
//        //TODO:完成下载功能
//        final DownloadManager manager = DownloadManager.getInstance();
//        manager.registerObserver(new DownloadManager.DownloadObserver() {
//            @Override
//            public void onDownloadStateChanged(final DownloadInfo info) {
//                if (info.getUrl().equals(urlPath)) {
//                    switch (info.getDownloadState()) {
//                        case DownloadState.STATE_ERROR:
//                            manager.unRegisterObserver(this);
//                            break;
//                        case DownloadState.STATE_DOWNLOADED:
//                            manager.unRegisterObserver(this);
//                            mPath = info.getPath();
//                            myHandler.sendEmptyMessage(0);
//                            break;
//                    }
//                }
//            }
//
//            @Override
//            public void onDownloadProgressed(DownloadInfo info) {
//            }
//
//        });
//        manager.download(urlPath);
//    }
//
//    /**
//     * 视频开始
//     */
//    public void start() {
//        uvVideo.start();
//    }
//
//    /**
//     * 视频暂停
//     */
//    public void pause() {
//        uvVideo.pause();
//    }
//
//    /**
//     * 设置播放按钮的点击事件
//     *
//     * @param l
//     */
//    public void setonPlayBtnClickListener(OnClickListener l) {
//        imgbtnPlay.setOnClickListener(l);
//    }
//
//    /**
//     * 设置视频准备完成的回调
//     *
//     * @param l
//     */
//    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
//        uvVideo.setOnPreparedListener(l);
//    }
//
//    /**
//     * 设置视频被播放完成的回调
//     *
//     * @param l
//     */
//    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
//        uvVideo.setOnCompletionListener(l);
//    }
//
//    /**
//     * 设置视频播放发生错误的回调
//     *
//     * @param l
//     */
//    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
//        uvVideo.setOnErrorListener(l);
//    }
//
//    /**
//     * 视频操作过程中的相关回调的全局变量
//     */
//    public VideoCallBack mVCallBack;
//
//    /**
//     * 设置视频过程中的相关回调
//     *
//     * @param callBack
//     */
//    public void setCallBack(VideoCallBack callBack) {
//        mVCallBack = callBack;
//        this.interceptTime = 2000;
//    }
//
//    /**
//     * 设置视频过程中的相关回调,定制拦截事件
//     *
//     * @param callBack
//     */
//    public void setCallBack(VideoCallBack callBack, int interceptTime) {
//        mVCallBack = callBack;
//        this.interceptTime = interceptTime;
//    }
//
//    /**
//     * 视频拦截回调接口
//     */
//    public interface VideoCallBack {
//        void onIntercept();
//    }
//
//    public interface VideoPlayCallBack {
//        void onPlay();
//    }
//
//    /**
//     * 设置点击开始播放按钮的回调
//     *
//     * @param callBack
//     */
//    public void setCallBack(VideoPlayCallBack callBack) {
//        //TODO:
//    }
//
//    /**
//     * 移除控制进度条的runable
//     */
//    public void removePbRunable() {
//        handler.removeCallbacks(runnable);
//    }
}
