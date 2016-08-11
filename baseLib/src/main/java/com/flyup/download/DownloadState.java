package com.flyup.download;

/**
 * Created by bruce on 2016/1/16.
 */
public interface   DownloadState {
    int STATE_NONE = 0;
    /** 等待中 */
    int STATE_WAITING = 1;
    /** 下载中 */
    int STATE_DOWNLOADING = 2;
    /** 暂停 */
    int STATE_PAUSED = 3;
    /** 下载完毕 */
    int STATE_DOWNLOADED = 4;
    /** 下载失败 */
    int STATE_ERROR = 5;
}
