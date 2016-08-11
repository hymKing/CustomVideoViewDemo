package com.flyup.common.utils;

import android.media.MediaPlayer;

import com.flyup.download.DownloadInfo;
import com.flyup.download.DownloadManager;
import com.flyup.download.DownloadState;

import java.io.File;
import java.io.IOException;

/**
  *
  * Created by Focux on 2016-4-16
  */
public class MediaPlayUtils implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {


    public void onCompletion(MediaPlayer mp) {
        stopPlay();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        stopPlay();
        setError(State.ERROR);
        return true;
    }

    public enum State {
        //正常状态
        IDLE_STATE/*起始，或播放结束*/, PLAYING_STATE/**播放中*/
        , PLAYING_PAUSED_STATE/**暂停中*/
        ,

        //播放错误状态
        ERROR, INTERNAL_ERROR, IN_CALL_RECORD_ERROR, DOWNLOAD_ERROR, PATH_ERROR/**路径为空或无效*/
    }

    private State mState = State.IDLE_STATE;


    public State state() {
        return mState;
    }

    public void setState(State state) {
        LogUtil.i("recorder", "播放状态切换  》》 " + state);
        if (state == mState)
            return;

        mState = state;
        signalStateChanged(mState);
    }

    public void setError(State error) {

        if (mOnStateChangedListener != null)
            mOnStateChangedListener.onError(error);
    }

    private void signalStateChanged(State state) {
        if (mOnStateChangedListener != null)
            mOnStateChangedListener.onStateChanged(state);
    }

    private OnStateChangedListener mOnStateChangedListener = null;

    public interface OnStateChangedListener {
        /***
         * @param state 正常状态
         */
        void onStateChanged(State state);

        /**
         * 播放进度
         *
         * @param msecPregress 当前播放位置的秒值
         * @param total        文件长度，秒
         * @param percent      播放进度百分比
         */
        void onPlayingProgress(int msecPregress, int total, float percent);

        /**
         * @param error 播放错误状态
         */
        void onError(State error);
    }


    /**
     * 播放进度
     */
    public float playProgress() {
        if (mPlayer != null) {
            return ((float) mPlayer.getCurrentPosition()) / mPlayer.getDuration();
        }
        return 0.0f;
    }

    /**
     * 监听播放状态
     **/
    public void setOnStateChangedListener(OnStateChangedListener listener) {
      /*  if (mOnStateChangedListener != null) {//通知前一个停止
            mOnStateChangedListener.onStateChanged(State.IDLE_STATE);
        }*/
        mOnStateChangedListener = listener;
    }

    private Runnable mUpdateProgress = new Runnable() {
        public void run() {
            if (mPlayer != null && state() == State.PLAYING_STATE) {
                if (mOnStateChangedListener != null) {
                    mOnStateChangedListener.onPlayingProgress(mPlayer.getCurrentPosition() / 1000, mPlayer.getDuration(), playProgress());
                }
                UIUtils.postDelayed(this, 200);
            }
        }
    };


    MediaPlayer mPlayer;


    /**
     * 播放本地文件，或网络文件
     */
    public void startPlay(String urlOrPath, OnStateChangedListener listener) {
        setOnStateChangedListener(listener);

        LogUtil.i("MediaPlayUtils", " start play >> " + urlOrPath);

        if (StringUtils.isEmpty(urlOrPath)) {
            setError(State.PATH_ERROR);
            return;
        }

        //本地文件
        File file = new File(urlOrPath);
        if (file.exists() && file.isFile()) {
            startPlay(file.getAbsolutePath());
            return;
        }

        final DownloadManager manager = DownloadManager.getInstance();
        LogUtil.i("MediaPlayUtils", " stxxxxxxart play >> " + urlOrPath);

        manager.registerObserver(new DownloadManager.DownloadObserver() {
            @Override
            public void onDownloadStateChanged(final DownloadInfo info) {
                switch (info.getDownloadState()) {
                    case DownloadState.STATE_ERROR:
                        //UIUtils.showToast("播放失败");
                        UIUtils.post(new Runnable() {
                            @Override
                            public void run() {
                                setError(State.DOWNLOAD_ERROR);
                            }
                        });
                        manager.unRegisterObserver(this);
                        break;
                    case DownloadState.STATE_DOWNLOADED:
                        LogUtil.i("MediaPlayUtils", "onSuccess responseInfo = " + info);
                        manager.unRegisterObserver(this);
                        UIUtils.post(new Runnable() {
                            @Override
                            public void run() {
                                startPlay(info.getPath());
                            }
                        });
                        break;
                }
            }

            @Override
            public void onDownloadProgressed(DownloadInfo info) {
            }
        });

        manager.download(urlOrPath);


//        API.downFile(urlOrPath, new RequestCallBack<File>() {
//            @Override
//            public void onSuccess(ResponseInfo<File> responseInfo) {
//                LogUtil.i("MediaPlayUtils", "onSuccess responseInfo = " + responseInfo);
//                startPlay(responseInfo.result.getAbsolutePath());
//            }
//
//            @Override
//            public void onFailure(HttpException error, String msg) {
//                LogUtil.i("MediaPlayUtils", " onFailure error = " + error + ", msg" + msg);
//                setError(State.DOWNLOAD_ERROR);
//            }
//        });
    }


    public void startPlay(String path) {
        try {
            if (state() == State.PLAYING_PAUSED_STATE) {
                mPlayer.seekTo(mPlayer.getCurrentPosition());
                mPlayer.start();
                setState(State.PLAYING_STATE);
            } else {
                stopPlay();

                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setVolume(0.7f,0.7f);
                    mPlayer.setDataSource(path);
                    mPlayer.setOnCompletionListener(this);
                    mPlayer.setOnErrorListener(this);
                    mPlayer.prepare();
                    mPlayer.seekTo(0);
                    mPlayer.start();
                } catch (IllegalArgumentException e) {
                    setError(State.INTERNAL_ERROR);
                    mPlayer = null;
                    return;
                } catch (IOException e) {
                    setError(State.ERROR);
                    mPlayer = null;
                    return;
                }
                UIUtils.post(mUpdateProgress);
                //mSampleStart = System.currentTimeMillis();
                setState(State.PLAYING_STATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setState(State.INTERNAL_ERROR);
        }
    }


    public void startPlay(int resId) {
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setVolume(0.7f,0.7f);
            mPlayer.setDataSource(UIUtils.getContext(), UIUtils.getResourceUri(resId));
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.prepare();
            mPlayer.seekTo(0);
            mPlayer.start();
        } catch (IllegalArgumentException e) {
            mPlayer = null;
            return;
        } catch (IOException e) {
            mPlayer = null;
            return;
        }
    }

    //暂停播放
    public void pausePlay() {
        if (mPlayer == null) {
            return;
        }

        mPlayer.pause();
        setState(State.PLAYING_PAUSED_STATE);
    }

    //停止播放，释放
    public void stopPlay() {
        if (mPlayer == null) // we were not in playback
            return;

        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        setState(State.IDLE_STATE);
    }
}
