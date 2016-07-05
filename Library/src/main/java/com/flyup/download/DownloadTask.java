package com.flyup.download;


import com.flyup.common.utils.LogUtil;
import com.flyup.net.OkHttp3ClientImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;


/**
 * 下载任务
 */
public class DownloadTask implements Runnable, DownloadState {
    private static final String TAG = "DownloadTask";
    private DownloadInfo info;
    private DownloadManager manager;


    public DownloadTask(DownloadInfo info, DownloadManager manager) {
        this.info = info;
        this.manager = manager;
    }

    private Request buildRequest(DownloadInfo info){
        return new Request.Builder()
                .url(info.getUrl())
                .tag(info.getId())
                .header("RANGE", "bytes=" + info.getCurrentSize() + "-")
                .build();
    }

    @Override
    public void run() {
        info.setDownloadState(STATE_DOWNLOADING);//先改变下载状态
        manager.notifyDownloadStateChanged(info);
        File file = new File(info.getPath()+".temp");//获取下载文件

        LogUtil.i(TAG,"create temp download file : "+ file);

        try {

            if (file.exists()) {
                if (info.getCurrentSize() == 0 || file.length() != info.getCurrentSize()) {
                    //如果文件不存在，或者进度为0，或者进度和文件长度不相符，就需要重新下载
                    info.setCurrentSize(0);
                    file.delete();
                }
            }else {//不存在，则确保从0开始
                //if (file.length() != info.getCurrentSize()) {
                    info.setCurrentSize(0);
                //}
            }

//            if (info.getCurrentSize() == 0 || file.exists() || file.length() != info.getCurrentSize()) {
//                //如果文件不存在，或者进度为0，或者进度和文件长度不相符，就需要重新下载
//                info.setCurrentSize(0);
//                file.delete();
//            }

            if (!file.exists()) {
                file.createNewFile();
            }

            Response resp = OkHttp3ClientImpl.execute(buildRequest(info));
            InputStream stream = null;
            if (resp == null || !resp.isSuccessful() || resp.body() == null || (stream = resp.body().byteStream()) == null) {
                info.setDownloadState(STATE_ERROR);//没有下载内容返回，修改为错误状态
                manager.notifyDownloadStateChanged(info);
            } else {
                info.setTotalSize(resp.body().contentLength());

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    int count = -1;
                    byte[] buffer = new byte[1024];
                    while (((count = stream.read(buffer)) != -1) && info.getDownloadState() == STATE_DOWNLOADING) {
                        //每次读取到数据后，都需要判断是否为下载状态，如果不是，下载需要终止，如果是，则刷新进度
                        fos.write(buffer, 0, count);
                        fos.flush();
                        info.setCurrentSize(info.getCurrentSize() + count);
                        manager.notifyDownloadProgressed(info);//刷新进度
                    }
                } catch (Exception e) {
                    LogUtil.e(e);//出异常后需要修改状态并删除文件
                    info.setDownloadState(STATE_ERROR);
                    manager.notifyDownloadStateChanged(info);
                    info.setCurrentSize(0);
                    file.delete();
                } finally {
                    IOUtils.close(fos);
                    if (resp.body() != null) {
                        resp.body().close();
                    }
                }
                //判断进度是否和app总长度相等
                if (info.getCurrentSize() == info.getTotalSize()) {
                    boolean renameTo = file.renameTo(new File(info.getPath()));
                    LogUtil.e(TAG,renameTo+"下载完成 ,,, renameTo " +file.getAbsolutePath());

                    info.setDownloadState(STATE_DOWNLOADED);
                    manager.notifyDownloadStateChanged(info);
                } else if (info.getDownloadState() == STATE_PAUSED) {//判断状态
                    manager.notifyDownloadStateChanged(info);
                } else {
                    info.setDownloadState(STATE_ERROR);
                    manager.notifyDownloadStateChanged(info);
                    info.setCurrentSize(0);//错误状态需要删除文件
                    file.delete();
                }
            }
            manager.mTaskMap.remove(info.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}