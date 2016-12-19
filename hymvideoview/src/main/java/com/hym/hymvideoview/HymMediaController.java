package com.hym.hymvideoview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Formatter;
import java.util.Locale;

/**
 * Desc:
 * <p/>
 * Desc:
 * <li>自定义的MediaController组件，作为视频播放的控制器
 * <li>此组件改造自原生的MediaController组件。
 * <p/>
 * <p/>
 * Created by Hym on 2016/7/5 10:27.<br/>
 * Modified by<br/>
 * Modification：<br/>
 * <p/>
 */
public class HymMediaController extends FrameLayout {
    private HymMediaController.MediaPlayerControl mPlayer;

    private Context mContext;

    private ProgressBar mProgress;

    private TextView mEndTime, mCurrentTime;

    private TextView mTitle;

    /**只标示底部的控制器是否是显示状态*/
    private boolean mShowing = true;

    private boolean mDragging;

    private boolean mScalable = false;
    private boolean mIsFullScreen = false;
//    private boolean mFullscreenEnabled = false;


    private static final int sDefaultTimeout = 3000;

    private static final int STATE_PLAYING = 1;
    private static final int STATE_PAUSE = 2;
    private static final int STATE_LOADING = 3;
    private static final int STATE_ERROR = 4;
    private static final int STATE_COMPLETE = 5;

    private int mState = STATE_LOADING;


    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int SHOW_LOADING = 3;
    private static final int HIDE_LOADING = 4;
    private static final int HIDE_LOADING_SHOW_PLAY=9;
    private static final int SHOW_ERROR = 5;
    private static final int HIDE_ERROR = 6;
    private static final int SHOW_COMPLETE = 7;
    private static final int HIDE_COMPLETE = 8;
    StringBuilder mFormatBuilder;

    Formatter mFormatter;

    private ImageButton mTurnButton;// 开启暂停按钮

    private ImageButton mScaleButton;

    private View mBackButton;// 返回按钮

    private ViewGroup loadingLayout;

    private ViewGroup errorLayout;

    private View mTitleLayout;
    /**底部的控制布局*/
    private View mControlLayout;
    /**控制器底部控制条是否显示开关属性*/
    private Boolean noBottomController=false;

    private View mCenterPlayButton;
    private View rootView;
    private TextView tvReplay;
    /**点击屏幕的时候，关闭当前全屏页面，目前全屏页面是使用activity去写的*/
    private boolean touchFinishSwitcher=false;

    public HymMediaController(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    public HymMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        //设置自定义组件的属性
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.HymMediaController);
        mScalable = a.getBoolean(R.styleable.HymMediaController_hv_scalable, false);
        a.recycle();
        init(context);
    }

    /**
     * 自定义组件的初始化
     *
     * @param context
     */
    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRoot = inflater.inflate(R.layout.hv_player_controller, this);
        viewRoot.setOnTouchListener(mTouchListener);
        initControllerView(viewRoot);
    }

    private void initControllerView(View v) {
        mTitleLayout = v.findViewById(R.id.title_part);
        mControlLayout = v.findViewById(R.id.control_layout);
        loadingLayout = (ViewGroup) v.findViewById(R.id.loading_layout);
        errorLayout = (ViewGroup) v.findViewById(R.id.error_layout);
        errorLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startVideo();
            }
        });
        mTurnButton = (ImageButton) v.findViewById(R.id.turn_button);
        mScaleButton = (ImageButton) v.findViewById(R.id.scale_button);
        mCenterPlayButton = v.findViewById(R.id.center_play_btn);
        mBackButton = v.findViewById(R.id.back_btn);
        rootView=v.findViewById(R.id.root_view);
        tvReplay=(TextView) v.findViewById(R.id.tv_replay);
        tvReplay.setOnClickListener(mCenterPlayListener);

        if (mTurnButton != null) {
            mTurnButton.requestFocus();
            mTurnButton.setOnClickListener(mPauseListener);
        }

        if (mScalable) {
            if (mScaleButton != null) {
                mScaleButton.setVisibility(VISIBLE);
                mScaleButton.setOnClickListener(mScaleListener);
            }
        } else {
            if (mScaleButton != null) {
                mScaleButton.setVisibility(GONE);
            }
        }

        if (mCenterPlayButton != null) {//重新开始播放
            mCenterPlayButton.setOnClickListener(mCenterPlayListener);
        }

        if (mBackButton != null) {//返回按钮仅在全屏状态下可见
            mBackButton.setOnClickListener(mBackListener);
        }

        View bar = v.findViewById(R.id.seekbar);
        mProgress = (ProgressBar) bar;
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.duration);
        mCurrentTime = (TextView) v.findViewById(R.id.has_played);
        mTitle = (TextView) v.findViewById(R.id.title);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    /**
     * 设置播放器点击屏幕关闭等相关需求，用于全屏页面
     * @param switcher
     */
    public void setTouchFinishSwitcher(boolean switcher){
        touchFinishSwitcher=switcher;
    }
    /**设置是否有底部播放控制条的属性*/
    public void setNoBottomController(Boolean noBottom){
        noBottomController=noBottom;
        mControlLayout.setVisibility(View.GONE);
    }

    //播放组件实现此接口，并得到实现此接口播放组件的引用
    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (mTurnButton != null && mPlayer != null && !mPlayer.canPause()) {
                mTurnButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     *显示底部和中间的控制按钮
     * @param timeout timeout 不为0的时候，到timeout的时间，会自动隐藏；为0的时候，一直显示到主动调用hide()方法，隐藏
     */
    public void show(int timeout) {
        Log.e("video_hide", "show()....");
        if (!mShowing) {
            //setProgress();
            if (mTurnButton != null) {
                mTurnButton.requestFocus();
            }
            disableUnsupportedButtons();
            mShowing = true;
        }
        updatePausePlay();
        updateBackButton();

        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
        if (mTitleLayout.getVisibility() != VISIBLE) {
            mTitleLayout.setVisibility(VISIBLE);
        }
        if (mControlLayout.getVisibility() != VISIBLE) {
            if (noBottomController) {
                mControlLayout.setVisibility(GONE);
            } else {
                mControlLayout.setVisibility(VISIBLE);
            }
        }
        //视频加载完成过后，重新加载，没有状态同步
        if (mPlayer != null && mPlayer.isPlaying()) {
            setCenterPlayBtnImgSourceAndTag(R.mipmap.hv_stop_btn);
        }
        showCenterView(lastCenterShowID);

        // cause the progress bar to be updated even if mShowing
        // was already true. This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        //mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if(!touchFinishSwitcher){
            if (timeout != 0) {
                mHandler.removeMessages(FADE_OUT);
                mHandler.sendMessageDelayed(msg, timeout);
           }
        }

    }

    public boolean isShowing() {
        return mShowing;
    }


    /**
     * 同时负责底部按钮和中间的按钮的显示和隐藏
     */
    public void hide() {
        Log.e("video_hide", "hide()....");
        if (mShowing) {
            //mHandler.removeMessages(SHOW_PROGRESS);
            mTitleLayout.setVisibility(GONE);
            //底部控制条隐藏
            if(!touchFinishSwitcher){
                mControlLayout.setVisibility(GONE);
            }
            //只有在播放状态的时候，底部按钮和中间按钮会同步显示和隐藏
            if (mPlayer != null && mPlayer.isPlaying()) {
                Log.e("video_hide","hide() center...");
                hideCenterView();
            }
            mShowing = false;
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT: //1
                    hide();
                    break;
                //显示进度条 ok
                case SHOW_PROGRESS: //2
                    pos = setProgress();
                    if (!mDragging&& mPlayer != null && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                case SHOW_LOADING: //3
                    //show();
                    showCenterView(R.id.loading_layout);
                    break;
                //显示重播按钮，ok
                case SHOW_COMPLETE: //7
                    if (!touchFinishSwitcher) {
                        setCenterPlayBtnImgSourceAndTag(R.mipmap.hv_itv_replay);
                        showCenterView(R.id.center_play_btn);
                        tvReplay.setVisibility(View.VISIBLE);
                        rootView.setBackgroundColor(Color.parseColor("#4c000000"));
                    }
                    updatePausePlay();
                    break;
                case SHOW_ERROR: //5
                    show(0);
                    showCenterView(R.id.error_layout);
                    break;
                case HIDE_LOADING: //4 缓冲完成进入播放状态的时候调用
                    hide();
                    Log.e("videoTest:", "_center_HIDE_LOADING do hide center view");
                    setCenterPlayBtnImgSourceAndTag(R.mipmap.hv_stop_btn);
                    lastCenterShowID= R.id.center_play_btn;
                    hideCenterView();
                    break;
                case HIDE_LOADING_SHOW_PLAY://9
                    hide();
                    Log.e("videoTest:", "_center_HIDE_LOADING_SHOW_PLAY do hide center view");
                    showCenterView(R.id.center_play_btn);
                    updatePausePlay();
                    setProgressInfo();
                    break;
                case HIDE_ERROR: //6
                    hide();
                    showCenterView(R.id.center_play_btn);
                    break;
                case HIDE_COMPLETE: //8
                    Log.e("videoTest:", "_center_HIDE_COMPLETE do hide center view");
                    hideCenterView();
            }
        }
    };

    //初始状态是播放按钮
    private int lastCenterShowID = R.id.center_play_btn;

    private void showCenterView(int resId) {
        Log.e("videoTest_center:", "do show center view");
        lastCenterShowID = resId;
        if (resId == R.id.loading_layout) {
            if (loadingLayout.getVisibility() != VISIBLE) {
                loadingLayout.setVisibility(VISIBLE);
            }
            if (mCenterPlayButton.getVisibility() == VISIBLE) {
                mCenterPlayButton.setVisibility(GONE);
            }
            if (errorLayout.getVisibility() == VISIBLE) {
                errorLayout.setVisibility(GONE);
            }
        } else if (resId == R.id.center_play_btn) {
            if(touchFinishSwitcher){
                return;
            }
            if (mCenterPlayButton.getVisibility() != VISIBLE) {
                mCenterPlayButton.setVisibility(VISIBLE);
            }
            if (loadingLayout.getVisibility() == VISIBLE) {
                loadingLayout.setVisibility(GONE);
            }
            if (errorLayout.getVisibility() == VISIBLE) {
                errorLayout.setVisibility(GONE);
            }

        } else if (resId == R.id.error_layout) {
            if (errorLayout.getVisibility() != VISIBLE) {
                errorLayout.setVisibility(VISIBLE);
            }
            if (mCenterPlayButton.getVisibility() == VISIBLE) {
                mCenterPlayButton.setVisibility(GONE);
            }
            if (loadingLayout.getVisibility() == VISIBLE) {
                loadingLayout.setVisibility(GONE);
            }

        }
    }


    private void hideCenterView() {
        Log.e("videoTest_center:", "do hide center view");
        if (mCenterPlayButton.getVisibility() == VISIBLE) {
            mCenterPlayButton.setVisibility(GONE);
        }
        if (errorLayout.getVisibility() == VISIBLE) {
            errorLayout.setVisibility(GONE);
        }
        if (loadingLayout.getVisibility() == VISIBLE) {
            loadingLayout.setVisibility(GONE);
        }
    }

    public void reset() {
        mCurrentTime.setText("00:00");
        mEndTime.setText("00:00");
        mProgress.setProgress(0);
        mTurnButton.setImageResource(R.mipmap.hv_player_player_btn);
        setVisibility(View.VISIBLE);
        hideLoadingShowPlay();
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }


    //mTouchListener 是设置在rootView上，先接受事件，OnTouchEvent 是在此自定义控件上，可以理解成最顶层layout，后接受事件
    boolean handled = false;
    //如果正在显示,则使之消失
    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if(touchFinishSwitcher&&mContext instanceof Activity){
                ((Activity)mContext).finish();
                return true;
            }
            Log.e("rootView","mTouchListener");
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mShowing) {
                    Log.e("rootView","mTouchListener"+mShowing);
                    hide();
                    handled = true;
                    return true;
                }
            }
            return false;
        }
    };
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e("rootView","onTouchEvent_down"+handled);
                handled = false;


                break;
            case MotionEvent.ACTION_UP:
                Log.e("rootView","onTouchEvent_up"+handled);
                if (!handled) {
                    handled = false;
                    show(sDefaultTimeout); // start timeout
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.e("rootView","onTouchEvent_cancel"+handled);
                hide();
                break;
            default:
                break;
        }
        return true;
    }



    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mTurnButton != null) {
                    mTurnButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                startVideo();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private void startVideo() {
        mPlayer.start();
        setProgressInfo();
    }

    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            tvReplay.setVisibility(View.GONE);
            if (mPlayer != null) {
                doPauseResume();
                show(sDefaultTimeout);
            }
        }
    };

    private OnClickListener mScaleListener = new OnClickListener() {
        public void onClick(View v) {
            mIsFullScreen = !mIsFullScreen;
            updateScaleButton();
            updateBackButton();
            //TODO:在为设置视频资源的时候，mPlayer并没有被初始化，会报空指针
            mPlayer.setFullscreen(mIsFullScreen);
        }
    };

    //仅全屏时才有返回按钮
    private OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            if (mIsFullScreen) {
                mIsFullScreen = false;
                updateScaleButton();
                updateBackButton();
                mPlayer.setFullscreen(false);
            }

        }
    };

    public OnClickListener mCenterPlayListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer == null) {
                return;
            }

            //
            hideCenterView();
            if (mPlayer.isPlaying()) {
                setCenterPlayBtnImgSourceAndTag(R.mipmap.hv_itv_player_play);
                mPlayer.pause();
                showCenterView(R.id.center_play_btn);
                updatePausePlay();
                return;
            }
            startVideo();
            rootView.setBackgroundColor(Color.parseColor("#00000000"));
            tvReplay.setVisibility(View.GONE);
            setCenterPlayBtnImgSourceAndTag(R.mipmap.hv_stop_btn);
            updatePausePlay();
        }
    };

    private void setProgressInfo() {
        setProgress();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }


    private void updatePausePlay() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mTurnButton.setImageResource(R.mipmap.hv_turn_stop_btn);
            //如果是重播状态，不同步
            if (mCenterPlayButton.getTag() != null && !(R.mipmap.hv_itv_replay + "").equals(mCenterPlayButton.getTag().toString()))
                setCenterPlayBtnImgSourceAndTag(R.mipmap.hv_stop_btn);
        } else {
            mTurnButton.setImageResource(R.mipmap.hv_player_player_btn);
            if (mCenterPlayButton.getTag() != null && !(R.mipmap.hv_itv_replay + "").equals(mCenterPlayButton.getTag().toString()))
                setCenterPlayBtnImgSourceAndTag(R.mipmap.hv_itv_player_play);
        }
    }

    private void setCenterPlayBtnImgSourceAndTag(int id) {
        ((ImageView) mCenterPlayButton).setTag(id);
        ((ImageView) mCenterPlayButton).setImageResource(id);
    }

    void updateScaleButton() {
        if (mIsFullScreen) {
            mScaleButton.setImageResource(R.mipmap.hv_star_zoom_in);
        } else {
            mScaleButton.setImageResource(R.mipmap.hv_player_scale_btn);
        }
    }

    void toggleButtons(boolean isFullScreen) {
        mIsFullScreen = isFullScreen;
        updateScaleButton();
        updateBackButton();
    }

    void updateBackButton() {
        mBackButton.setVisibility(mIsFullScreen ? View.VISIBLE : View.INVISIBLE);
    }

    boolean isFullScreen() {
        return mIsFullScreen;
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            setCenterPlayBtnImgSourceAndTag(R.mipmap.hv_itv_player_play);
        } else {
            setCenterPlayBtnImgSourceAndTag(R.mipmap.hv_stop_btn);
            startVideo();
        }
        updatePausePlay();
    }


    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        int newPosition = 0;

        boolean change = false;

        public void onStartTrackingTouch(SeekBar bar) {
            if (mPlayer == null) {
                return;
            }
            show(3600000);

            mDragging = true;
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null || !fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            newPosition = (int) newposition;
            change = true;
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (mPlayer == null) {
                return;
            }
            if (change) {
                mPlayer.seekTo(newPosition);
                if (mCurrentTime != null) {
                    mCurrentTime.setText(stringForTime(newPosition));
                }
            }
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mShowing = true;
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
//        super.setEnabled(enabled);
        if (mTurnButton != null) {
            mTurnButton.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        if (mScalable) {
            mScaleButton.setEnabled(enabled);
        }
        mBackButton.setEnabled(true);// 全屏状态下右上角的返回键总是可用.
    }

    public void showLoading() {
        mHandler.sendEmptyMessage(SHOW_LOADING);
    }

    public void hideLoading() {
        mHandler.sendEmptyMessage(HIDE_LOADING);
    }
    public void hideLoadingShowPlay() {
        mHandler.sendEmptyMessage(HIDE_LOADING_SHOW_PLAY);
    }

    public void showError() {
        mHandler.sendEmptyMessage(SHOW_ERROR);
    }

    public void hideError() {
        mHandler.sendEmptyMessage(HIDE_ERROR);
    }

    public void showComplete() {
        mHandler.sendEmptyMessage(SHOW_COMPLETE);
    }

    public void hideComplete() {
        mHandler.sendEmptyMessage(HIDE_COMPLETE);
    }

    public void setTitle(String titile) {
        mTitle.setText(titile);
    }

//    public void setFullscreenEnabled(boolean enabled) {
//        mFullscreenEnabled = enabled;
//        mScaleButton.setVisibility(mIsFullScreen ? VISIBLE : GONE);
//    }


    public void setOnErrorView(int resId) {
        errorLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(resId, errorLayout, true);
    }

    public void setOnErrorView(View onErrorView) {
        errorLayout.removeAllViews();
        errorLayout.addView(onErrorView);
    }

    public void setOnLoadingView(int resId) {
        loadingLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(resId, loadingLayout, true);
    }

    public void setOnLoadingView(View onLoadingView) {
        loadingLayout.removeAllViews();
        loadingLayout.addView(onLoadingView);
    }

    public void setOnErrorViewClick(OnClickListener onClickListener) {
        errorLayout.setOnClickListener(onClickListener);
    }

    /**
     * HymMediaController中内部声明的播放控制接口，由播放组件实现，有控制器HymMediaController调用
     */
    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        void closePlayer();//关闭播放视频,使播放器处于idle状态

        void setFullscreen(boolean fullscreen);

        /***
         * @param fullscreen
         * @param screenOrientation valid only fullscreen=true.values should be one of
         *                          ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
         *                          ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
         *                          ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,
         *                          ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
         */
        void setFullscreen(boolean fullscreen, int screenOrientation);
    }
}
