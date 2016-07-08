package com.hym.hymvideoview;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.VideoView;

import java.io.IOException;
import java.util.Map;

/**
 * Desc:
 * <p>
 * <li>自定义的videoView，定制了自定义的HymMediaController类
 * <li>此组件改造自原生的VideoView组件
 * <p/>
 * <p>
 * Created by Hym on 2016/7/5 10:27.<br/>
 * Modified by<br/>
 * Modification：<br/>
 * <p/>
 */
public class HymVideoView extends SurfaceView implements  HymMediaController.MediaPlayerControl,
        OrientationDetector.OrientationChangeListener{
    private String TAG = "HymVideoView";
    private Uri mUri;
    //根据原生MediaPlayer的生命周期状态图，定义出如下可能的所有的生命周期状态值
    /**Error状态，视频资源异常等错误情况出现时，产生*/
    private static final int STATE_ERROR              =-1;
    /**Idle状态，空闲状态，组件刚初始化时候的状态*/
    private static final int STATE_IDLE               = 0;
    /**Preparing状态，调用了prepare()or prepareAsyc()方法后到prepared状态之间的状态*/
    private static final int STATE_PREPARING          = 1;
    /**调用了prepare() or prepareAsyc(),并完成准备（包括缓冲区的准备）的状态。*/
    private static final int STATE_PREPARED           = 2;
    /**调用了start()方法后，正常播放的状态*/
    private static final int STATE_PLAYING            = 3;
    /**调用了pause()方法后，进入暂停状态*/
    private static final int STATE_PAUSED             = 4;
    /**调用了start()方法后，视频播放完成进入状态*/
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    /**当前状态*/
    private int mCurrentState = STATE_IDLE;
    /**目标状态，下一个要进入的状态*/
    private int mTargetState  = STATE_IDLE;

    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;

    //TODO:DOC
    private int         mAudioSession;
    /**视频源的宽度*/
    private int         mVideoWidth;
    /**视频源的高度*/
    private int         mVideoHeight;
    /**surfaceView的宽度*/
    private int         mSurfaceWidth;
    /**surfaceView的高度*/
    private int         mSurfaceHeight;


    private HymMediaController mMediaController;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    /**当前缓冲区的比例*/
    private int         mCurrentBufferPercentage;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnInfoListener mOnInfoListener;

    /**记录prepare过程中的seek的位置*/
    private int         mSeekWhenPrepared;
    private boolean     mCanPause;//准备完成后设置成true
    private boolean     mCanSeekBack;//准备完成后设置成true
    private boolean     mCanSeekForward;//准备完成后设置成true
    /**调用start方法之前，判断是否已经准备完成，true：已经准备完成，在准备完成的回调中设置成true*/
    private boolean     mPreparedBeforeStart;

    private Context mContext;
    private boolean     mFitXY = false;
    private boolean     mAutoRotation = false;
    /**
     * true:表示横屏全屏，false 标示竖屏全屏
     */
    private boolean     mFullScreenDirection=false;
    private int  mVideoViewLayoutWidth = 0;
    private int  mVideoViewLayoutHeight = 0;

    private OrientationDetector mOrientationDetector;
    private VideoViewCallback videoViewCallback;
    public HymVideoView(Context context) {
        this(context,null);
    }

    public HymVideoView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public HymVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //设置两个自定义属性
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.HymVideoView,0,0);
        mFitXY = a.getBoolean(R.styleable.HymVideoView_hv_fitXY, false);
        mAutoRotation = a.getBoolean(R.styleable.HymVideoView_hv_autoRotation, false);
        mFullScreenDirection=a.getBoolean(R.styleable.HymVideoView_hv_fullScreen_direction,false);
        a.recycle();
        initVideoView();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mFitXY) {
            onMeasureFitXY(widthMeasureSpec, heightMeasureSpec);
        } else {
            onMeasureKeepAspectRatio(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void onMeasureFitXY(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private void onMeasureKeepAspectRatio(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")");

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if ( mVideoWidth * height  < width * mVideoHeight ) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if ( mVideoWidth * height  > width * mVideoHeight ) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(HymVideoView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            info.setClassName(HymVideoView.class.getName());
        }
    }
    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        return getDefaultSize(desiredSize, measureSpec);
    }
    private void initVideoView(){
        mVideoWidth=0;
        mVideoHeight=0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //TODO:?
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        //初始化相关状态
        mCurrentState=STATE_IDLE;
        mTargetState=STATE_IDLE;
    }
    @Override
    public void onOrientationChanged(int screenOrientation, OrientationDetector.Direction direction) {
        if (!mAutoRotation) {
            return;
        }

        if (direction == OrientationDetector.Direction.PORTRAIT) {
            setFullscreen(false, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (direction == OrientationDetector.Direction.REVERSE_PORTRAIT) {
            setFullscreen(false, ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else if (direction == OrientationDetector.Direction.LANDSCAPE) {
            setFullscreen(true, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (direction == OrientationDetector.Direction.REVERSE_LANDSCAPE) {
            setFullscreen(true, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }
    public void setFitXY(boolean fitXY) {
        mFitXY = fitXY;
    }

    public void setAutoRotation(boolean auto) {
        mAutoRotation = auto;
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }
    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState  = STATE_IDLE;
        }
    }

    /**打开视频*/
    public void openVideo(){
        if (mUri == null || mSurfaceHolder == null) {
            return;
        }
        //请求音频焦点
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        //target state 不清除，因为之前可能已经开始播放
        release(false);
        try{
            mMediaPlayer = new MediaPlayer();

            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mContext, mUri);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);


            /*Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mMediaPlayer!=null)*/
                    mMediaPlayer.prepareAsync();
         /*       }
            },10000);*/
            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        }catch (IOException ex){
            Log.e(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }

    }

    /**设置MeidaPlayer控制器*/
    public void setMediaController(HymMediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    /**将MediaController绑定到MediaPlayer上*/
    private void attachMediaController() {

        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            mMediaController.setEnabled(isInPlaybackState());
            mMediaController.hide();
        }
    }

    ///////////////////////////以下是内部的相关事件监听的声明///////////////////////
    /**video 的大小第一次被知晓，或者video的大小被更新的时候回调
     * setDataSource方法之后，会调用，得到视频源的大小*/
    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    Log.e(TAG, String.format("onVideoSizeChanged width=%d,height=%d", mVideoWidth, mVideoHeight));
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        //根据视频资源设置surfaceView绘制的分辨率
                        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                        requestLayout();
                    }
                }
            };
    /**调用prepare方法，准备完成之后的回调*/
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;

            mCanPause = mCanSeekBack = mCanSeekForward = true;

            mPreparedBeforeStart = true;
            if (mMediaController != null) {
                mMediaController.hideLoading();
            }

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            //值为0时，是在没有视屏源，没有展示的surfaceView被设置。或者视频源的大小，还没有被决定
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the video here instead of in the callback.
                    if (mTargetState == STATE_PLAYING) {
                        start();
                        if (mMediaController != null) {
                            mMediaController.show();
                        }
                    } else if (!isPlaying() &&
                            (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (mMediaController != null) {
                            // Show the media controls when we're paused into a video and make 'em stick.
                            mMediaController.show(0);
                        }
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    /**视频播放完成的回调*/
    private MediaPlayer.OnCompletionListener mCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;
                    if (mMediaController != null) {
                        boolean a = mMediaPlayer.isPlaying();
                        int b = mCurrentState;
                        mMediaController.showComplete();
                        //FIXME 播放完成后,视频中央会显示一个播放按钮,点击播放按钮会调用start重播,
                        // 但start后竟然又回调到这里,导致第一次点击按钮不会播放视频,需要点击第二次.
                        Log.e(TAG, "播放完成"+String.format("a=%s,b=%d", a, b));
                    }
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }
            };
    /**视频缓冲区等信息改变的回调*/
    private MediaPlayer.OnInfoListener mInfoListener =
            new MediaPlayer.OnInfoListener() {
                public  boolean onInfo(MediaPlayer mp, int what, int extra){
                    boolean handled = false;
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            Log.e(TAG, "onInfo MediaPlayer.MEDIA_INFO_BUFFERING_START");
                            if (videoViewCallback != null) {
                                videoViewCallback.onBufferingStart(mMediaPlayer);
                            }
                            if (mMediaController != null) {
                                mMediaController.showLoading();
                            }
                            handled = true;
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            Log.e(TAG, "onInfo MediaPlayer.MEDIA_INFO_BUFFERING_END");
                            if (videoViewCallback != null) {
                                videoViewCallback.onBufferingEnd(mMediaPlayer);
                            }
                            if (mMediaController != null) {
                                mMediaController.hideLoading();
                            }
                            handled = true;
                            break;
                    }
                    if (mOnInfoListener != null) {
                        return mOnInfoListener.onInfo(mp, what, extra) || handled;
                    }
                    return handled;
                }
            };

    public int getmCurrentState() {
        return mCurrentState;
    }

    private MediaPlayer.OnErrorListener mErrorListener =
            new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                    Log.e(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;
                    if (mMediaController != null) {
                        mMediaController.showError();
                    }

            /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }
                    //删除了源码中的视频出现异常的dialog
                    return true;
                }
            };
    /**视频缓冲区更新的进度回调*/
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };

    /**
     * 设置准备完成的监听器，在内部监听器中合适时机调用
     * @param l
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)
    {
        mOnPreparedListener = l;
    }

    /**
     * 设置播放完成的监听器
     * @param l
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l)
    {
        mOnCompletionListener = l;
    }

    /**
     * 设置错误发生的监听器
     * @param l
     */
    public void setOnErrorListener(MediaPlayer.OnErrorListener l)
    {
        mOnErrorListener = l;
    }

    //TODO:?
   /**设置相关信息的监听器*/
    public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }
    /**surface callback*/
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int w, int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState =  (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
            Log.e("videoTest","surfaceChanged");
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.e("videoTest","surfaceCreated");
            mSurfaceHolder = holder;
            openVideo();
            enableOrientationDetect();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mMediaController != null) mMediaController.hide();
            release(true);
            disableOrientationDetect();
            Log.e("videoTest","surfaceDestroyed");
        }
    };
    private void enableOrientationDetect() {
        if (mAutoRotation && mOrientationDetector == null) {
            mOrientationDetector = new OrientationDetector(mContext);
            mOrientationDetector.setOrientationChangeListener(HymVideoView.this);
            mOrientationDetector.enable();
        }
    }

    private void disableOrientationDetect() {
        if (mOrientationDetector != null) {
            mOrientationDetector.disable();
        }
    }

    /**
     * 释放MediaPlayer的相关资源，参数是false的时候，不释放目标状态
     * @param cleartargetstate
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState  = STATE_IDLE;
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisibility();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisibility();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisibility();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisibility() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public void start() {
        if(getmCurrentState()==STATE_ERROR&&mUri!=null){
            setVideoURI(mUri);
        }
        if (!mPreparedBeforeStart && mMediaController != null) {
            mMediaController.showLoading();
        }else{
            mMediaController.hideComplete();
        }
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            mMediaController.hideComplete();
            if (this.videoViewCallback != null) {
                this.videoViewCallback.onStart(mMediaPlayer);
            }
        }
        mTargetState = STATE_PLAYING;
    }

    /**判断是否在播放状态*/
    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }


    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
                if (this.videoViewCallback != null) {
                    this.videoViewCallback.onPause(mMediaPlayer);
                }
            }
        }
        mTargetState = STATE_PAUSED;
    }
    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }
    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(pos);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = pos;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public void closePlayer() {
        release(true);
    }

    @Override
    public void setFullscreen(boolean fullscreen) {
        int screenOrientation = fullscreen ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if(mFullScreenDirection){
            screenOrientation=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }else{
            screenOrientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        setFullscreen(fullscreen, screenOrientation);
    }

    @Override
    public void setFullscreen(boolean fullscreen, int screenOrientation) {
        // Activity需要设置为: android:configChanges="keyboardHidden|orientation|screenSize"
        Activity activity = (Activity) mContext;

        if (fullscreen) {
            if (mVideoViewLayoutWidth == 0 && mVideoViewLayoutHeight == 0) {
                ViewGroup.LayoutParams params = getLayoutParams();
                mVideoViewLayoutWidth = params.width;//保存全屏之前的参数
                mVideoViewLayoutHeight = params.height;
            }
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.setRequestedOrientation(screenOrientation);
        } else {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = mVideoViewLayoutWidth;//使用全屏之前的参数
            params.height = mVideoViewLayoutHeight;
            setLayoutParams(params);

            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.setRequestedOrientation(screenOrientation);
        }
        mMediaController.toggleButtons(fullscreen);
        if (videoViewCallback != null) {
            videoViewCallback.onScaleChange(fullscreen);
        }
    }

    //////////////////////////////////以下监听可以不使用////////////////////////////////
    public interface VideoViewCallback {
        void onScaleChange(boolean isFullscreen);
        void onPause(final MediaPlayer mediaPlayer);
        void onStart(final MediaPlayer mediaPlayer);
        void onBufferingStart(final MediaPlayer mediaPlayer);
        void onBufferingEnd(final MediaPlayer mediaPlayer);
    }

    public void setVideoViewCallback(VideoViewCallback callback) {
        this.videoViewCallback = callback;
    }
}
